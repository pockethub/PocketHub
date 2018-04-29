node {
  // Mark the code checkout 'stage'....
  stage 'Stage Checkout'

  // Checkout code from repository and update any submodules
  checkout scm
  sh 'git submodule update --init'  

  stage 'Stage Build'

  //branch name from Jenkins environment variables
  echo "My branch is: ${env.BRANCH_NAME}"

  sh "./gradlew clean build"

  stage 'Stage Archive'
  //tell Jenkins to archive the apks
  archiveArtifacts artifacts: 'app/build/outputs/apk/*.apk', fingerprint: true
}
