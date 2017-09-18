def configuration

node ('android-test') {
    step([$class: 'WsCleanup', notFailBuild: true])
    stage('Checkout') {
        // Check out code
        //properties([disableConcurrentBuilds()])
        properties([disableConcurrentBuilds(),[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '20', daysToKeepStr: '20', numToKeepStr: '20']]]);


        checkout scm

        configuration = load 'scripts/jenkins/main/branch.groovy'
        checkout = load 'scripts/jenkins/steps/checkout.groovy'
        common = load 'scripts/jenkins/lib/common.groovy'
        gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
        bupa = load 'scripts/jenkins/steps/bupa.groovy'

        // Post-checkout prep
        checkout.exportGitEnvVars()
        //checkout.checkoutBackbone()
        sh 'env | sort'

        // stash the entire checkout including .git dir
        stash(name: 'sources', useDefaultExcludes: false)
        stash(name: 'pipeline', includes: 'scripts/jenkins/**,scripts/git-status/*.jar')
        gitStatus.reportGitStatus('Jenkins Job', 'Running job...', 'pending')



        step([$class: 'WsCleanup', notFailBuild: true])
    }
}

configuration.execute()
