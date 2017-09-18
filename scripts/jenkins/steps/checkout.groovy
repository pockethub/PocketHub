def exportGitEnvVars() {
    def issue
    sh 'git rev-parse HEAD > commit'
    String gitCommit = readFile 'commit'
    env.GIT_COMMIT = gitCommit.trim()
    // BRANCH_NAME env var available in multi-branch pipeline
    script = '''
    echo $CHANGE_BRANCH | egrep -o '([a-zA-Z][a-zA-Z0-9_]+-[1-9][0-9]*)([^.]|\\.[^0-9]|\\.\\$|\\$)'
    '''
    try {
        issue = sh(script: script, returnStdout: true).trim()
    } catch (error) {
        echo error.message
        //error ('Can't work out JIRA issue from branch name'')
    } finally {
        echo "Jira Issue Key: ${issue}"
    }

    env.JIRA_ISSUE = issue
    // Java8 is configured in Manage Jenkins -> Global Tool Configuration
    env.JAVA_HOME="${tool 'Java8'}"
    env.JAVA7_HOME = env.JAVA_HOME
    env.ANDROID_HOME='/usr/lib/android-sdk'
}

def checkoutBackbone() {
    sh 'aws s3 cp s3://babylon-bitrise/`git submodule | sed -E \'s/.([a-z0-9]*).*/\\1/g\'`-babylon.zip dist-babylon.zip'
    sh 'aws s3 cp s3://babylon-bitrise/`git submodule | sed -E \'s/.([a-z0-9]*).*/\\1/g\'`-bupa.zip dist-bupa.zip'
    stash(name:'backbone-babylon', includes:'dist-babylon.zip')
    stash(name:'backbone-bupa', includes:'dist-bupa.zip')
    sh 'rm dist-*.zip'
}

return this
