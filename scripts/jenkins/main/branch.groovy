// this is called from ${project.root}/Jenkinsfile
// this pipeline to build feature branches (initially)

def execute() {

    def checks
    def gitStatus
    def common
    def keys
    def bupa
    def gitBranch

    node('android') {

        unstash 'sources'
        gitBranch = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
        gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
        checks = load 'scripts/jenkins/steps/checks.groovy'
        common = load 'scripts/jenkins/lib/common.groovy'
        keys = load 'scripts/jenkins/lib/keys.groovy'
        bupa = load 'scripts/jenkins/steps/bupa.groovy'

        step([$class: 'WsCleanup', notFailBuild: true])
    }

    common.config.fastDexguardBuilds = true

    parallel(
            'Unit tests': {
                checks.unitTests(false)
            },
            'Checkstyle': {
                checks.lint()
            },
            'Lint': {
                checks.checkstyle()
            }
    )

    milestone(label: 'Finished testing!')

    checks.publishReports()

    echo "Job result : ${currentBuild.result}"




    stage('Package') {

        // V2 => build release.
        // TODO refactor this ENV-273
        if (env.BRANCH_NAME ==~ /^(v2|develop)/) {
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

        } else if (env.BRANCH_NAME ==~ /^(hotfix|release)\/.*/) {
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
        } else {
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
        }
    }

    echo "Job result : ${currentBuild.result}"
    milestone(label: 'Finished packaging!')


    stage('Finish') {
        node('android') {
            unstash 'pipeline'
            echo "Job result : ${currentBuild.result}"
            common.reportFinalBuildStatus()
            common.slackFeed()
        }
    }
}

return this
