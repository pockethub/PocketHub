ukReleaseKeys = [
        [$class: 'StringBinding', credentialsId: 'ANDROID_PLAYSTORE_UK_STORE_PASS', variable: 'RELEASE_STORE_PASS'],
        [$class: 'StringBinding', credentialsId: 'ANDROID_PLAYSTORE_UK_KEY_PASS', variable: 'RELEASE_KEY_PASS'],
        [$class: 'StringBinding', credentialsId: 'ANDROID_PLAYSTORE_UK_KEY_ALIAS', variable: 'RELEASE_KEY_ALIAS'],
        [$class: 'FileBinding', credentialsId: 'ANDROID_PLAY_STORE_UK_KEYSTORE', variable: 'RELEASE_KEYSTORE_LOCATION']
]

hockeyUploadKey = [
        [$class: 'StringBinding', credentialsId: 'HOCKEY_API_TOKEN', variable: 'HOCKEY_API_TOKEN']
]

return this
