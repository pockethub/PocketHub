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
        error(">>> Step ${context} failed! <<<")

    } finally {
        postBuildStep.call()
    }
}

void reportGitStatus(String context, String description, String status) {

    if (env.CHANGE_ID) {
        try {
            githubNotify account: 'Babylonpartners', context: "$context", credentialsId: 'GITHUB-PERSONAL-ACCESS-USER-PASS', description: "${description}", gitApiUrl: '', repo: 'babylon-android', sha: "${env.GIT_COMMIT}", status: "${status}", targetUrl: ''
        } catch (error) {
            echo ">>> Github reporting failed ... : ${error.message} <<<"
        }
    }
}

return this
