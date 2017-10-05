def release(ukReleaseKeys) {
    parallel(
            'Build-uk-qa (release)': {
                node('android') {
                    common.prepareWorkspace()
                    gitStatus.gitStatusEnabled(('Build-uk-qa'), {
                        sh "./gradlew assembleUkQa ${common.gradleParameters()}"
                        common.archiveCommonArtifacts()
                        common.hockeyUpload('**/*uk-qa.apk', 'f1b730748cc24c1381e401e19464a6d7')
                    }, {})
                }
            },
            'Build-uk-release (release)': {
                node('android') {
                    withCredentials(ukReleaseKeys) {
                        common.prepareWorkspace()
                        gitStatus.gitStatusEnabled(('Build-uk-release'), {
                            sh "./gradlew assembleUkRelease ${common.gradleParameters()} -PbuildForPlayStore=true"
                            common.archiveCommonArtifacts()
                            common.hockeyUpload('**/*uk-release.apk', '57ee0adad0f1458facae0a97b610e57c')
                        }, {})
                    }
                }
            },
            'Build-bupa-qa (release)': {
                node('android') {
                    bupa.prepareWorkspace()
                    gitStatus.gitStatusEnabled(('Build-bupa-qa'), {
                        sh "./gradlew assembleBupaQa ${common.gradleParameters()}"
                        common.archiveCommonArtifacts()
                        common.hockeyUpload('**/*bupa-qa.apk', 'fce5e16218164f218ef29c3027c0bf9d')
                    }, {})
                }
            },
            'Build-bupa-release (release)': {
                node('android') {
                    withCredentials(ukReleaseKeys) {
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
}

def develop(ukReleaseKeys) {
    parallel(
            'Build-uk-qa (develop)': {
                node('android') {
                    common.prepareWorkspace()
                    gitStatus.gitStatusEnabled(('Build-uk-qa'), {
                        sh "./gradlew assembleUkQa ${common.gradleParameters()}"
                        common.archiveCommonArtifacts()
                        common.hockeyUpload('**/*uk-qa.apk', '64a4e9ebce0143e4b69bba8dfb5aa45b')
                    }, {})
                }
            },
            'Build-uk-release (develop)': {
                node('android') {
                    withCredentials(ukReleaseKeys) {
                        common.prepareWorkspace()
                        gitStatus.gitStatusEnabled(('Build-uk-release'), {
                            sh "./gradlew assembleUkRelease ${common.gradleParameters()} -PbuildForPlayStore=true"
                            common.archiveCommonArtifacts()
                            common.hockeyUpload('**/*uk-release.apk', 'ce8afc86748c44439436747e9bc36092')
                        }, {})
                    }
                }
            },
            'Build-bupa-qa (develop)': {
                node('android') {
                    bupa.prepareWorkspace()
                    gitStatus.gitStatusEnabled(('Build-bupa-qa'), {
                        sh "./gradlew assembleBupaQa ${common.gradleParameters()}"
                        common.archiveCommonArtifacts()
                        common.hockeyUpload('**/*bupa-qa.apk', 'ab1c214e64d840d6ab35cad6485b45b9')
                    }, {})
                }
            },
            'Build-bupa-release (develop)': {
                node('android') {
                    withCredentials(ukReleaseKeys) {
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
}

def pullRequest(ukReleaseKeys) {
    parallel(
            'Build-uk-qa (PR)': {
                node('android') {
                    common.prepareWorkspace()

                    gitStatus.gitStatusEnabled(('Build-uk-qa'), {
                        sh "./gradlew assembleUkQa ${common.gradleParameters()}"
                        common.archiveCommonArtifacts()
                        common.hockeyUpload('**/*.apk', 'cc0df4bdadd44e7ebfe0d4c0d3e34566')
                    }, {})

                }
            }
//            'Build-uk-release (PR)': {
//                node('android') {
//                    common.prepareWorkspace()
//                    gitStatus.gitStatusEnabled(('Build-uk-release'), {
//                        sh "./gradlew assembleUkRelease ${common.gradleParameters()}"
//                        common.archiveCommonArtifacts()
//                        common.hockeyUpload('**/*.apk', '64a9adf1beea41c292ca51a80abc11e2')
//                    }, {})
//                }
//            }
    )
}

return this