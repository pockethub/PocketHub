config = [
        fastDexguardBuilds: false
]

def prepareWorkspace() {
    deleteDir()
    unstash 'sources'
    unstash 'backbone-babylon'
    sh 'mv dist-babylon.zip app/src/main/assets/dist-babylon.zip'
    sh '''
        cd app/src/main/assets
        unzip dist-babylon.zip
    '''
}

def printDaemonStatus() {
    sh './gradlew --status'
}

def stashWorkspace() {
    stash(name: 'sources', excludes: 'backbone/**,**/dist/**')
}

def hockeyUpload(String apkName, String appId) {
    def keys = load 'scripts/jenkins/lib/keys.groovy'
    withCredentials(keys.hockeyUploadKey) {
        step([$class: 'HockeyappRecorder', applications: [[apiToken: env.HOCKEY_API_TOKEN, downloadAllowed: true, filePath: apkName, mandatory: false, notifyTeam: false, releaseNotesMethod: [$class: 'ChangelogReleaseNotes'], uploadMethod: [$class: 'VersionCreation', appId: appId]]], debugMode: true, failGracefully: false])
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
             slackSend channel: 'android_feed', color: color, message: "Build ${result} - ${env.GIT_BRANCH} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", teamDomain: 'babylonhealth', token: env.ANDROID_SLACK_INTEGRATION_KEY
         }
     } catch (error) {
         // this is not fatal just annoying
         echo "Slack feed updated failed!"
     }
}

def reportFinalBuildStatus() {
    unstash 'pipeline'
    def gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
    def body = """
        Build Succeeded!...
        Build Number: ${env.BUILD_NUMBER}
        Jenkins URL: ${env.BUILD_URL}
        Git Commit: ${env.GIT_COMMIT}
        Git Branch: ${env.GIT_BRANCH}
        """
    echo "Job result : ${currentBuild.result}"
    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
        gitStatus.reportGitStatus('Jenkins Job', 'Job successful!', 'success')
        common.notifyJira(body, "${env.JIRA_ISSUE}")
    } else {
        gitStatus.reportGitStatus('Jenkins Job', 'Job failed!', 'failure')

        common.notifyJira("Build Failed!" , "${env.JIRA_ISSUE}")
    }
}


def notifyJira(String message, String key) {
    if ( key != 'None') {
        try {
            jiraComment body: message, issueKey: key
        } catch (error) {
            echo "Caught: ${error}"
        }
    }
}

def gradleParameters() {
    "-PcustomVersionCode=${env.BUILD_NUMBER} -PjenkinsFastDexguardBuildsEnabled=${config.fastDexguardBuilds} -Dorg.gradle.java.home=${env.JAVA_HOME} -Pandroid.enableBuildCache=true --project-cache-dir=${env.WORKSPACE}/.gradle/cache -PtestCoverageFlag=true --profile"
}

def archiveCommonArtifacts() {
    archive '**/*mapping.txt,**/reports/**'
}

def archiveGradleCrashLogs() {
    archive 'hs_err_*,**/hs_err_*'
}

return this
