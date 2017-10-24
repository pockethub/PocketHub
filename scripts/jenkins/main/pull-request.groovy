def execute() {
    def checks
    def gitStatus
    def common
    def bupa

    node('android') {
        unstash 'sources'

        // Load your utility scripts here
        gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
        checks = load 'scripts/jenkins/steps/checks.groovy'
        common = load 'scripts/jenkins/lib/common.groovy'
        bupa = load 'scripts/jenkins/steps/bupa.groovy'
        common.config.fastDexguardBuilds = true
    }
    parallel(
            'Unit tests': {
                node('android') {
                    common.prepareWorkspace()
                    checks.unitTests(false)
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
            'Build-bupa-qa': {
                node('android') {
                    bupa.prepareWorkspace()
                    gitStatus.gitStatusEnabled(('Build-bupa-qa'), {
                        sh "./gradlew assembleBupaQa ${common.gradleParameters()}"
                        common.archiveCommonArtifacts()
                        common.hockeyUpload('**/*bupa-qa.apk', '3462c35a8cf145e39e573314d8fc632d')
                    }, {})
                }
            },
            'Build-uk-qa': {
                node('android') {
                    common.prepareWorkspace()
                    gitStatus.gitStatusEnabled(('Build-uk-qa'), {
                        sh "./gradlew assembleUkQa ${common.gradleParameters()}"
                        common.archiveCommonArtifacts()
                        common.hockeyUpload('**/*uk-qa.apk', '64a9adf1beea41c292ca51a80abc11e2')
                    }, {})
                }
            },
            'Build-uk-release': {
                node('android') {
                    common.prepareWorkspace()
                    gitStatus.gitStatusEnabled(('Build-uk-release'), {
                        sh "./gradlew assembleUkRelease ${common.gradleParameters()}"
                        archive '**/reports/profile/**/*'
                    }, {})
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
