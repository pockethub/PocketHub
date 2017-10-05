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
        throw RuntimeException()

    } finally {
        postBuildStep.call()
    }
}

def getGitHubSHA(changeId) {
    try {
        withCredentials([[$class: 'StringBinding', credentialsId: 'github', variable: 'GITHUB_TOKEN']]) {

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

void reportGitStatus(String context, String description, String status) {

    try {
        githubNotify account: 'devopsworksio', context: "$context", credentialsId: 'devopsworksio', description: "${description}", gitApiUrl: '', repo: 'PocketHub', sha: "${env.GIT_COMMIT}", status: "${status}", targetUrl: ''
    } catch (error) {
        echo "### Github reporting failed ... : ${error.message}"
    }

}

return this
