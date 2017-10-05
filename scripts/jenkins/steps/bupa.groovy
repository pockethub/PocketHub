def prepareWorkspace() {
    step([$class: 'WsCleanup', notFailBuild: true])
    unstash 'workspace'
    unstash 'backbone-bupa'
    sh 'rm -fr app/src/main/assets/dist'
    sh 'unzip -o dist-bupa.zip -d app/src/main/assets'

}

return this
