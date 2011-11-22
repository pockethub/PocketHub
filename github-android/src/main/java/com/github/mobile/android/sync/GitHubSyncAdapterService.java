package com.github.mobile.android.sync;

import com.google.inject.Inject;

import android.content.Intent;
import android.os.IBinder;
import roboguice.inject.ContextScopedProvider;
import roboguice.service.RoboService;

public class GitHubSyncAdapterService extends RoboService {

    @Inject
    ContextScopedProvider<GitHubSyncAdapter> syncAdapterProvider;

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapterProvider.get(this).getSyncAdapterBinder();
    }
}