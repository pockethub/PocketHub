def gitStatusEnabled(String context, Closure buildStep, Closure postBuildStep) {
    reportGitStatus(context, "Running $context...", "pending")
    try {
        buildStep.call()
        println (">>> Build Step $context passed! <<<")
        reportGitStatus(context, "$context passed!", "success")

        currentBuild.result = 'SUCCESS'
    } catch (error) {
        reportGitStatus(context, "$context failed!", "failure")
        common.notifyJira("Build Failed! ${error.message} ..." , "${env.JIRA_ISSUE}")
        currentBuild.result = 'FAILURE'
        // this will stop the build and mark it as failed
        error (">>> Build failed in ${error.message}...! <<<")
    } finally {
        postBuildStep.call()
    }
}

void reportGitStatus(String context, String description, String status) {

    try {
        githubNotify account: 'devopsworksio', context: "$context", credentialsId: 'devopsworksio', description: "${description}", gitApiUrl: '', repo: 'PocketHub', sha: "${env.GIT_COMMIT}", status: "${status}", targetUrl: ''
    } catch (error) {
        echo "### Github reporting failed ... : ${error.message}"
    }

}

return this
