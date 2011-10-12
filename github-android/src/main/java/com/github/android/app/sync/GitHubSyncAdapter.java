package com.github.android.app.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import roboguice.inject.ContextScope;

@Singleton
class GitHubSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SA";

    @Inject
    public GitHubSyncAdapter(Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
		Log.d(TAG, "Really, I should be syncing stuff");
    }

    @Override
    public void onSyncCanceled() {
        // cancelAnyCurrentCampaign();
    }

}
