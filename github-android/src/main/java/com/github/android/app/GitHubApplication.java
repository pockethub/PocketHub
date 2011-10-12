package com.github.android.app;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;
import com.google.inject.Module;
import roboguice.application.RoboApplication;

import java.util.List;

import static java.util.Arrays.asList;

public class GitHubApplication extends RoboApplication {

	public static final String TAG = "GHA";

	public GitHubApplication() {
	}

	public GitHubApplication(Context context) {
		attachBaseContext(context);
	}

	public GitHubApplication(Instrumentation instrumentation) {
		attachBaseContext(instrumentation.getTargetContext());
	}

	protected void addApplicationModules(List<Module> modules) {
		Log.i(TAG, "Adding application modules...");
		modules.addAll(asList(new GitHubModule()));
	}
}
