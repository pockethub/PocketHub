def unitTests(boolean runMainTestsOnSecondaryVariants) {
    stage('Unit tests') {
        node('android') {
            step([$class: 'WsCleanup', notFailBuild: true])
            unstash 'sources'
            def gitStatus = load 'scripts/jenkins/lib/git-status.groovy'

            def unzip = '''
            rm -fr app/src/main/assets/dist
            mkdir -p app/src/main/assets/dist
            '''
            try {
                stdout = sh(returnStdout: true, script: unzip)
            } catch (error) {
                echo "${error}"
                println stdout
                error('>>> Unzip failed! <<<')
            }

            gitStatus.gitStatusEnabled(('Unit tests'), {
                sh "./gradlew testSuiteWithCoverage ${common.gradleParameters()} -PrunMainTestsForSecondaryVariants=${runMainTestsOnSecondaryVariants.toString()}"
            }, {
                stash(name: 'junit-report', includes: '**/TEST*.xml', alllowEmpy: false)
                stash(name: 'jacoco-exec', includes: '**/jacoco/*.exec', alllowEmpy: false)

            })
            stash(name: 'workspace', useDefaultExcludes: true)
            step([$class: 'WsCleanup', notFailBuild: true])
        }
    }
}

def publishReports() {
    stage('Publish reports') {
        node('android') {
            step([$class: 'WsCleanup', notFailBuild: true])
            try {
                unstash 'junit-report'
                step([$class: 'JUnitResultArchiver', testResults: '**/TEST*.xml'])

            } catch (error) {
                echo 'Junit report publishing failed for: ' + error.message
            }

            try {
                step([$class: 'JacocoPublisher', execPattern: '**/jacoco/*.exec'])
            } catch (error) {
                echo 'Jacoco report publishing failed for: ' + error.message

            } finally {
                step([$class: 'WsCleanup', notFailBuild: true])
            }
        }
    }

}

def checkstyle() {
    stage('Checkstyle') {
        node('android') {
            step([$class: 'WsCleanup', notFailBuild: true])

            unstash 'sources'
            def gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
            gitStatus.gitStatusEnabled(('Checkstyle'), {
                sh "./gradlew checkstyle ${common.gradleParameters()}"
            }, {
                step([$class: 'CheckStylePublisher', canComputeNew: false, canRunOnFailed: true, defaultEncoding: '', failedTotalAll: '9999', healthy: '', pattern: '**/checkstyle.xml', unHealthy: '1'])
            })


        }
    }
}

def lint() {
    stage('Lint') {
        node('android') {
            step([$class: 'WsCleanup', notFailBuild: true])
            unstash 'sources'

        def gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
            gitStatus.gitStatusEnabled(('Lint'), {
                sh "./gradlew lintSuite ${common.gradleParameters()}"
            }, {
                step([$class: 'LintPublisher', canComputeNew: false, canRunOnFailed: true, defaultEncoding: '', healthy: '0', pattern: '', unHealthy: '30', useStableBuildAsReference: true])
            })


        }
    }
}

return this
