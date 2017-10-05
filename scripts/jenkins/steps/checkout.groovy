def exportGitEnvVars() {
    def issue
    def commit
    def get_issue
    def get_commit

    // doing this in groovy runs into problems with the groovy sandbox security checks
    if (env.CHANGE_ID) {
        get_issue = '''
        echo ${CHANGE_BRANCH} | grep -Po '(?!\\/)([a-zA-Z]+-[0-9]+)(?!\\d)'
        '''
        env.GIT_COMMIT = common.getGitHubSHA(env.CHANGE_ID)
        echo ' >>> this is a PR build !<<<'

    } else {
        get_issue = '''
        echo ${BRANCH_NAME} | grep -Po '(?!\\/)([a-zA-Z]+-[0-9]+)(?!\\d)'
        echo ' >>> this is a BRANCH build !<<<'
        '''
    }

    sh 'env | sort'

    try {
        issue = sh(script: get_issue, returnStdout: true).trim()
        echo "Jira Issue Key: ${issue}"
        env.JIRA_ISSUE = issue
    } catch (error) {
        echo ">>> Error! cant't work out JIRA issue from branch name. This is not fatal but please adhere to naming convention!"
        echo "${error.message}"
    }

    // Java8 is configured in Manage Jenkins -> Global Tool Configuration
    env.JAVA_HOME = "${tool 'Java8'}"
    env.JAVA7_HOME = env.JAVA_HOME
    env.ANDROID_HOME = '/opt/android-sdk-linux'


}

def checkoutBackbone() {
    sh 'aws s3 cp s3://babylon-bitrise/`git submodule | sed -E \'s/.([a-z0-9]*).*/\\1/g\'`-babylon.zip dist-babylon.zip'
    sh 'aws s3 cp s3://babylon-bitrise/`git submodule | sed -E \'s/.([a-z0-9]*).*/\\1/g\'`-bupa.zip dist-bupa.zip'
    stash(name: 'backbone-babylon', includes: 'dist-babylon.zip')
    stash(name: 'backbone-bupa', includes: 'dist-bupa.zip')
    sh 'rm dist-*.zip'
}

return this
