def prepareWorkspace() {
    deleteDir()
    unstash 'workspace'
    unstash 'backbone-bupa'
    sh 'rm -fr app/src/main/assets/dist'
    sh 'unzip dist-bupa.zip -d app/src/main/assets'

}

return this
