package com.github.android.app.sync;

import android.content.Intent;
import android.os.IBinder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import roboguice.service.RoboService;

public class GitHubSyncAdapterService extends RoboService {

	@Inject Provider<GitHubSyncAdapter> syncAdapterProvider;

	@Override
	public IBinder onBind(Intent intent) {
        return syncAdapterProvider.get().getSyncAdapterBinder();
	}
}