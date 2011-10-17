package com.github.mobile.android;

import static java.util.Arrays.asList;
import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

import com.google.inject.Module;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import roboguice.application.RoboApplication;
import shade.org.apache.commons.logging.LogFactory;

public class GitHubApplication extends RoboApplication {

	private static final String TAG = "GHA";

	public GitHubApplication() {
		registerLog();
	}

	public GitHubApplication(Context context) {
		registerLog();
		attachBaseContext(context);
	}

	public GitHubApplication(Instrumentation instrumentation) {
		registerLog();
		attachBaseContext(instrumentation.getTargetContext());
	}

	/**
	 * Registers a custom LogFactory that forwards to the Android Log class
	 */
	private void registerLog() {
		AccessController.doPrivileged(new PrivilegedAction<String>() {
			public String run() {
				return System.setProperty(LogFactory.class.getName(),
						com.github.mobile.android.LogFactory.class.getName());
			}
		});
	}

	protected void addApplicationModules(List<Module> modules) {
		Log.i(TAG, "Adding application modules...");
		modules.addAll(asList(new GitHubModule()));
	}
}
