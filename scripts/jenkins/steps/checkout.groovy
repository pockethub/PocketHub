def exportGitEnvVars() {
    sh 'git rev-parse HEAD > commit'
    //sh 'git branch --list --contains > branch'
    sh 'git branch -a --contains $(git rev-parse HEAD)'
    sh 'git branch -a --contains $(git rev-parse HEAD) | grep "remotes" > branch'
    String gitCommit = readFile 'commit'
    env.GIT_COMMIT = gitCommit.trim()
    String gitBranch = readFile 'branch'
    env.GIT_BRANCH = gitBranch.trim()
    env.JAVA_HOME = '/usr/lib/jvm/java-8-openjdk-amd64/' // Set the proper JDK on Jenkins
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
