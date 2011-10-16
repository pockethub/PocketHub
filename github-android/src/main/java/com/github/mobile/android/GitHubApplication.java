package com.github.mobile.android;

import static java.util.Arrays.asList;
import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

import com.google.inject.Module;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import roboguice.application.RoboApplication;

public class GitHubApplication extends RoboApplication {

	public static final String TAG = "GHA";

	public GitHubApplication() {
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				return System.setProperty(LogFactory.class.getName(),
						com.github.mobile.android.LogFactory.class.getName());
			}
		});
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
