def configuration
node('android') {
    stage('Checkout') {
        // Check out code
        deleteDir()
        checkout scm

        // Load scripts depending on the configuration
        configuration = load "scripts/jenkins/main/${PARAM_CONFIGURATION}.groovy"
        checkout = load 'scripts/jenkins/steps/checkout.groovy'
        common = load 'scripts/jenkins/lib/common.groovy'
        gitStatus = load 'scripts/jenkins/lib/git-status.groovy'

        // Post-checkout prep
        checkout.exportGitEnvVars()
        checkout.checkoutBackbone()
        common.stashWorkspace()

    }
}
configuration.execute()
