// this is called from ${project.root}/Jenkinsfile
// this pipeline to build feature branches (initially)

def execute() {

    def checks
    def gitStatus
    def common
    def keys
    def bupa
    def gitBranch

    node('android-test') {

        // TODO: fix this - expensive
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
        lock(inversePrecedence: true, quantity: 1, resource: "android-pipeline-${gitBranch}") {
            parallel(

                    // TODO: this needs refactoring!
                    'Build-uk-qa': {
                        node('android-test') {
                            step([$class: 'WsCleanup', notFailBuild: true])
                            unstash 'workspace'
                            gitStatus.gitStatusEnabled(('Build-uk-qa'), {
                                try {
                                    sh "./gradlew assembleDebug ${common.gradleParameters()}"

                                } catch (error) {
                                    error("${error}")
                                } finally {
                                    common.archiveCommonArtifacts()
                                    step([$class: 'WsCleanup', notFailBuild: true])
                                }
                            }, {
                                //
                            })
                        },
                        'Build-uk-release': {
                            node('android-test') {
                                step([$class: 'WsCleanup', notFailBuild: true])

                                unstash 'workspace'
                                gitStatus.gitStatusEnabled(('Build-uk-release'), {
                                    try {
                                        sh "./gradlew assembleDebug ${common.gradleParameters()}"

                                    } catch (error) {
                                        error("${error}")
                                    } finally {
                                        common.archiveCommonArtifacts()
                                        step([$class: 'WsCleanup', notFailBuild: true])
                                    }
                                    //common.hockeyUpload('**/*uk-release.apk', 'ce8afc86748c44439436747e9bc36092')
                                }, {

                                })
                            }
                        },

                        )
                    }
        }

        echo "Job result : ${currentBuild.result}"

        milestone(label: 'Finished packaging!')

        node('android-test') {
            unstash 'pipeline'
            echo "Job result : ${currentBuild.result}"
            common.reportFinalBuildStatus()
            //common.slackFeed()
        }
    }

    return this
