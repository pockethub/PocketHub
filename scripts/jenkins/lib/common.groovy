import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import hudson.model.*


config = [
        fastDexguardBuilds: false
]


def getGitHubSHA(changeId) {
    try {
        // TODO: parameterize
        withCredentials([[$class: 'StringBinding', credentialsId: 'github', variable: 'GITHUB_TOKEN']]) {

            def apiUrl = "https://api.github.com/repos/devopsworksio/PocketHub/pulls/${changeId}"
            def response = sh(returnStdout: true, script: "curl -s -H \"Authorization: Token ${env.GITHUB_TOKEN}\" -H \"Accept: application/json\" -H \"Content-type: application/json\" -X GET ${apiUrl}").trim()
            def jsonSlurper = new JsonSlurper()
            def data = jsonSlurper.parseText("${response}")
            return data.head['sha']
        }
    } catch (error) {
        echo "${error}"
        echo "${response}"
        error("Failed to get GitHub SHA for PR")
    }
}


def prepareWorkspace() {
    deleteDir()
    unstash 'sources'

}

def printDaemonStatus() {
    sh './gradlew --status'
}

def stashWorkspace() {
    stash(name: 'sources', excludes: 'backbone/**,**/dist/**')
}


def hockeyUpload(String apkName, String appId) {

    withCredentials([string(credentialsId: 'HOCKEY_JENKINS_API_TOKEN', variable: 'HOCKEY_API_TOKEN')]) {
        echo " >>> Hockeyapp uploading ${apkName} <<<"
        try {
            step([$class: 'HockeyappRecorder', applications: [[apiToken: env.HOCKEY_API_TOKEN, downloadAllowed: true, filePath: apkName, mandatory: false, notifyTeam: false, releaseNotesMethod: [$class: 'ChangelogReleaseNotes'], uploadMethod: [$class: 'VersionCreation', appId: appId]]], debugMode: true, failGracefully: false])
        } catch (error) {
            error( "Error! >>> Failed to upload ${apkName} - ${error} <<<")

        }
        finally {
            sh 'env | grep HOCKEY'
        }
    }
}


def slackFeed(result) {
    def color
    def msg
    def icon
    switch (result) {
        case 'SUCCESS':
            color = 'good'
            icon = ':beer:'
            break
        case 'UNSTABLE':
            color = "warning"
            break
        case 'FAILED':
            colour = 'danger'
            icon = ':-1:'
            break
        default:
            colour = 'danger'
    }

    // PRs have this URL  releases don't
    if (env.CHANGE_URL) {
        msg = """
    Build  ${result} ${icon}
    GitHub: <${env.CHANGE_URL}|${env.BRANCH_NAME}> 
    Jenkins Build:  <${env.BUILD_URL}|(click)>
    Jira Issue: <https://babylonpartners.atlassian.net/browse/${env.JIRA_ISSUE}|${env.JIRA_ISSUE}>
    Hockey Version: ${env.BUILD_COUNTER}
    """

    } else {
        msg = "Build ${result} - <${env.BUILD_URL}|Jenkins>"
    }

    try {
        withCredentials([[$class: 'StringBinding', credentialsId: 'ANDROID_SLACK_INTEGRATION_KEY', variable: 'ANDROID_SLACK_INTEGRATION_KEY']]) {
            slackSend channel: 'android_feed', color: color, message: msg, teamDomain: 'babylonhealth', token: env.ANDROID_SLACK_INTEGRATION_KEY
        }
    } catch (error) {
        // this is not fatal just annoying
        echo ">>> Slack feed updated failed! <<<"
    }
}

def reportFinalBuildStatus() {

    unstash 'pipeline'
    def gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
    def body = """
        Build Succeeded!...
        Hockey Version: ${env.BUILD_COUNTER}
        Jenkins URL: ${env.BUILD_URL}
        Git Hub URL: ${env.CHANGE_URL}
        Git Commit: ${env.GIT_COMMIT}
        """
    echo "Job result : ${currentBuild.result}"
    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
        gitStatus.reportGitStatus('Jenkins Job', 'Job successful!', 'SUCCESS')
        common.notifyJira(body, env.JIRA_ISSUE)

    } else {
        gitStatus.reportGitStatus('Jenkins Job', 'Job failed!', 'FAILURE')
        common.notifyJira("Build Failed!", env.JIRA_ISSUE)
    }

}


def notifyJira(String message, String key) {
    if (key != 'None' || key != null) {
        try {
            jiraComment body: message, issueKey: key
        } catch (error) {
            echo ">>> JIRA Notification failed! ${error} <<<"
        }
    }
}

def gradleParameters() {
    return  "-PjenkinsFastDexguardBuildsEnabled=${config.fastDexguardBuilds} " +
            "-Dorg.gradle.java.home=${env.JAVA_HOME} " +
            "-Pandroid.enableBuildCache=false " +
            "-PtestCoverageFlag=true " +
            "--profile --no-daemon"
}

def gradleParametersWithVersion() {
    if (env.BUILD_COUNTER == null) {
        error (">>> env.BUILD_COUNTER can not be null. Stopping pipeline. Please consult the logs for the root cause. <<<")

    }
    params = gradleParameters()
    return "-PcustomVersionCode=${env.BUILD_COUNTER}  " + params
}

def archiveCommonArtifacts() {
    archive '**/*mapping.txt,**/reports/**'
}

def archiveGradleCrashLogs() {
    archive 'hs_err_*,**/hs_err_*'
}

@NonCPS
def buildCounter() {
    build 'build-counter'
    def job = Jenkins.instance.getItemByFullName('build-counter')
    def item = job.getLastSuccessfulBuild().number
    return item

}

return this
