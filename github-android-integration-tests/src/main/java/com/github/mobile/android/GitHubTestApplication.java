package com.github.mobile.android;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

public class GitHubTestApplication extends GitHubApplication {
    private static final String TAG = "GitHubTestApplication";

    public GitHubTestApplication(Instrumentation instrumentation) {
        super(instrumentation);
        Log.i(TAG, "GETTING CALLED with instrumentation...");
    }

    public GitHubTestApplication(Context context) {
        super(context);
        Log.i(TAG, "REALLY GETTING CALLED!!");
    }
}
