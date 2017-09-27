import groovy.json.JsonOutput
import groovy.json.JsonSlurper

config = [
        fastDexguardBuilds: false
]

def prepareWorkspace() {
    step([$class: 'WsCleanup', notFailBuild: true])

    unstash 'workspace'
    unstash 'backbone-babylon'

    def unzip = '''
    rm -fr app/src/main/assets/dist
    unzip dist-babylon.zip -d app/src/main/assets
    '''

    try {
        stdout = sh(returnStdout: true, script: unzip)
    } catch (error) {
        echo "${error}"
        println stdout
        error('>>> Error! Unzip of backbone failed <<<')
    }
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
            echo "Error! >>> Failed to upload ${apkName} - ${error} <<<"
            throw new Exception()
        }
    }
}

def getGitHubSHA(changeId) {
    try {
        withCredentials([string(credentialsId: 'github', variable: 'GITHUB_TOKEN')]) {

            def apiUrl = "https://api.github.com/repos/babylonpartners/babylon-android/pulls/${changeId}"
            def response = sh(returnStdout: true, script: "curl -s -H \"Authorization: Token ${env.GITHUB_TOKEN}\" -H \"Accept: application/json\" -H \"Content-type: application/json\" -X GET ${apiUrl}").trim()
            def jsonSlurper = new JsonSlurper()
            def data = jsonSlurper.parseText("${response}")
            return data.head['sha']
        }
    } catch (error) {
        echo "${error}"
        error("Failed to get GitHub SHA for PR")
    }
}

def slackFeed() {
    String color;
    String result;
    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
        color = 'good'
        result = 'successful'
    } else if (currentBuild.result == 'UNSTABLE') {
        color = 'warning'
        result = 'unstable'
    } else {
        color = 'danger'
        result = 'failed!'
    }

    try {
        withCredentials([[$class: 'StringBinding', credentialsId: 'ANDROID_SLACK_INTEGRATION_KEY', variable: 'ANDROID_SLACK_INTEGRATION_KEY']]) {
            slackSend channel: 'android_feed', color: color, message: "Build ${result} - ${env.BRANCH_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", teamDomain: 'babylonhealth', token: env.ANDROID_SLACK_INTEGRATION_KEY
        }
    } catch (error) {
        // this is not fatal just annoying
        echo ">>> Slack feed updated failed! <<<"
    }
}

def reportFinalBuildStatus() {
    stage('Finish') {
        unstash 'pipeline'
        def gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
        def body = """
        Build Succeeded!...
        Build Number: ${env.BUILD_NUMBER}
        Jenkins URL: ${env.BUILD_URL}
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
    "-PcustomVersionCode=${env.BUILD_NUMBER} -PjenkinsFastDexguardBuildsEnabled=${config.fastDexguardBuilds} -Dorg.gradle.java.home=${env.JAVA_HOME} -Pandroid.enableBuildCache=false -PtestCoverageFlag=true --profile --no-daemon"
}

def archiveCommonArtifacts() {
    archive '**/*mapping.txt,**/reports/**'
}

def archiveGradleCrashLogs() {
    archive 'hs_err_*,**/hs_err_*'
}

return this
