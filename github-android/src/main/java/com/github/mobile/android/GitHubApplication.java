package com.github.mobile.android;

import static java.util.Arrays.asList;
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

    protected void addApplicationModules(List<Module> modules) {
        Log.i(TAG, "Adding application modules...");
        modules.addAll(asList(new GitHubModule()));
    }
}
