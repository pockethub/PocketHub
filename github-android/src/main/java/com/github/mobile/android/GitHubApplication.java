package com.github.mobile.android;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

import com.google.inject.Module;

import java.util.List;

/**
 * Main GitHub application
 */
public class GitHubApplication extends Application {

    private static final String TAG = "GHA";

    /**
     * Create main application
     */
    public GitHubApplication() {
    }

    /**
     * Create main application
     *
     * @param context
     */
    public GitHubApplication(Context context) {
        attachBaseContext(context);
    }

    /**
     * Create main application
     *
     * @param instrumentation
     */
    public GitHubApplication(Instrumentation instrumentation) {
        attachBaseContext(instrumentation.getTargetContext());
    }

    /**
     * Add modules
     *
     * @param modules
     */
    protected void addApplicationModules(List<Module> modules) {
        Log.d(TAG, "Adding application modules...");
        modules.add(new GitHubModule());
    }
}
