def gitStatusEnabled(String context, Closure buildStep, Closure postBuildStep) {
    reportGitStatus(context, "Running $context...", "PENDING")
    try {
        buildStep.call()
        reportGitStatus(context, "$context passed!", "SUCCESS")
        currentBuild.result = 'SUCCESS'
    } catch (error) {
        reportGitStatus(context, "$context failed!", "FAILURE")
        common.notifyJira("Build Failed! See ${env.BUILD_URL} for details.", "${env.JIRA_ISSUE}")
        currentBuild.result = 'FAILURE'
        println ">>> Step ${context} failed! <<<"
        // stops the pipeline
        error

    } finally {
        postBuildStep.call()
    }
}

void reportGitStatus(String context, String description, String status) {

    //TODO: parameterise
    try {
        githubNotify account: 'devopsworksio', context: "$context", credentialsId: 'github-username-password', description: "${description}", gitApiUrl: '', repo: 'PocketHub', sha: "${env.GIT_COMMIT}", status: "${status}", targetUrl: ''
    } catch (error) {
        echo ">>> Github reporting failed ... : ${error.message} <<<"
    }

}

return this
