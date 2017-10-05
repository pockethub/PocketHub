def execute() {
    def checks
    def gitStatus
    def common
    def keys

    node('android') {
        unstash 'sources'

        // Load your utility scripts here
        gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
        checks = load 'scripts/jenkins/steps/checks.groovy'
        common = load 'scripts/jenkins/lib/common.groovy'
        bupa = load 'scripts/jenkins/steps/bupa.groovy'
        keys = load 'scripts/jenkins/lib/keys.groovy'
    }
    parallel(
            'Unit tests': {
                node('android') {
                    common.prepareWorkspace()
                    checks.unitTests(true)
                    common.stashWorkspace() // Save partial build artifacts
                }
            },
            'Checkstyle & Lint': {
                node('android') {
                    common.prepareWorkspace()
                    checks.checkstyle()
                    checks.lint()
                }
            }
    )
    parallel(
            'Build-uk-qa': {
                node('android') {
                    common.prepareWorkspace()
                    gitStatus.gitStatusEnabled(('Build-uk-qa'), {
                        sh "./gradlew assembleUkQa ${common.gradleParameters()}"
                        common.archiveCommonArtifacts()
                        common.hockeyUpload('**/*uk-qa.apk', 'f1b730748cc24c1381e401e19464a6d7')
                    }, {})
                }
            },
            'Build-uk-release': {
                node('android') {
                    withCredentials(keys.ukReleaseKeys) {
                        common.prepareWorkspace()
                        gitStatus.gitStatusEnabled(('Build-uk-release'), {
                            sh "./gradlew assembleUkRelease ${common.gradleParameters()} -PbuildForPlayStore=true"
                            common.archiveCommonArtifacts()
                            common.hockeyUpload('**/*uk-release.apk', '57ee0adad0f1458facae0a97b610e57c')
                        }, {})
                    }
                }
            },
            'Build-bupa-qa': {
                node('android') {
                    bupa.prepareWorkspace()
                    gitStatus.gitStatusEnabled(('Build-bupa-qa'), {
                        sh "./gradlew assembleBupaQa ${common.gradleParameters()}"
                        common.archiveCommonArtifacts()
                        common.hockeyUpload('**/*bupa-qa.apk', 'fce5e16218164f218ef29c3027c0bf9d')
                    }, {})
                }
            },
            'Build-bupa-release': {
                node('android') {
                    withCredentials(keys.ukReleaseKeys) {
                        bupa.prepareWorkspace()
                        gitStatus.gitStatusEnabled(('Build-bupa-release'), {
                            sh "./gradlew assembleBupaRelease ${common.gradleParameters()} -PbuildForPlayStore=true"
                            common.archiveCommonArtifacts()
                            common.hockeyUpload('**/*bupa-release.apk', '12cd8f493ca74a45adf6b549ef8e91fb')
                        }, {})
                    }
                }
            }
    )
    node('android') { // Needs to be executed within a node context as it uses credentials
        common.prepareWorkspace()
        common.reportFinalBuildStatus()
        common.slackFeed()
    }
}

return this
