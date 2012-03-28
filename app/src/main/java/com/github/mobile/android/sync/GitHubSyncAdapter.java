package com.github.mobile.android.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.google.inject.Inject;

import roboguice.inject.ContextScope;
import roboguice.inject.ContextSingleton;

@ContextSingleton
class GitHubSyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject
    private ContextScope contextScope;
    @Inject
    private SyncCampaign.Factory syncCampaignFactory;

    private SyncCampaign currentSyncCampaign = null;

    @Inject
    public GitHubSyncAdapter(Context context) {
        super(context, true);
    }

    /**
     * @param ignoredAccount - note this parameter is currently ignored, but we should be syncing *this* account
     */
    @Override
    public void onPerformSync(Account ignoredAccount, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        contextScope.enter(getContext());
        try {
            cancelAnyCurrentCampaign();
            currentSyncCampaign = syncCampaignFactory.createCampaignFor(syncResult);
            currentSyncCampaign.run();
        } finally {
            contextScope.exit(getContext());
        }
    }

    @Override
    public void onSyncCanceled() {
        cancelAnyCurrentCampaign();
    }

    private void cancelAnyCurrentCampaign() {
        if (currentSyncCampaign != null)
            currentSyncCampaign.cancel();
    }

}
