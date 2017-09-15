def gitStatusEnabled(String context, Closure buildStep, Closure postBuildStep) {
    reportGitStatus(context, "Running $context...", "pending")
    try {
        buildStep.call()
        println (">>> Build Step $context passed! <<<")
        reportGitStatus(context, "$context passed!", "success")

        currentBuild.result = 'SUCCESS'
    } catch (err) {
        reportGitStatus(context, "$context failed!", "failure")
        common.notifyJira("Build Failed! ${err} ..." , "${env.JIRA_ISSUE}")
        currentBuild.result = 'FAILURE'
        // this will stop the build and mark it as failed
        error (">>> Build failed in ${err.message}...! <<<")
    } finally {
        postBuildStep.call()
    }
}

void reportGitStatus(String context, String description, String status) {

    withCredentials([string(credentialsId: 'JENKINS_PERSONAL_ACCESS_TOKEN', variable: 'JENKINS_PERSONAL_ACCESS_TOKEN')]) {
        sh 'env | grep JENKINS'
        try {
            sh "java -jar ./scripts/git-status/kotStatus.jar ${status} --context=\"${context}\" --description=\"${description}\""
        } catch (err) {
            echo "### Github reporting failed ... : ${err.message}"
        }
    }
}

return this
