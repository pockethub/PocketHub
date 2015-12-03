package com.github.pockethub;

import android.app.Application;

import com.alorma.github.basesdk.client.credentials.GithubDeveloperCredentials;
import com.alorma.github.basesdk.client.credentials.MetaDeveloperCredentialsProvider;

public class PocketHub extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GithubDeveloperCredentials.init(new MetaDeveloperCredentialsProvider(getApplicationContext()));
    }
}
