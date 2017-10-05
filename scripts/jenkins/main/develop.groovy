def execute() {
    def checks
    def gitStatus
    def common
    def bupa
    def keys

    node('android') {
        unstash 'sources'

        // Load your utility scripts here
        gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
        checks = load 'scripts/jenkins/steps/checks.groovy'
        common = load 'scripts/jenkins/lib/common.groovy'
        keys = load 'scripts/jenkins/lib/keys.groovy'
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
            'Build-uk-qa': {
                node('android') {
                    common.prepareWorkspace()
                    gitStatus.gitStatusEnabled(('Build-uk-qa'), {
                        sh "./gradlew assembleUkQa ${common.gradleParameters()}"
                        common.archiveCommonArtifacts()
                        common.hockeyUpload('**/*uk-qa.apk', '64a4e9ebce0143e4b69bba8dfb5aa45b')
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
                            common.hockeyUpload('**/*uk-release.apk', 'ce8afc86748c44439436747e9bc36092')
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
                        common.hockeyUpload('**/*bupa-qa.apk', 'ab1c214e64d840d6ab35cad6485b45b9')
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
                            common.hockeyUpload('**/*bupa-release.apk', '53669713793c4da1a69bf0afb3e19588')
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
