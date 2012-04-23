package com.github.mobile.android.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.github.mobile.android.guice.GitHubAccountScope;
import com.google.inject.Inject;

import roboguice.inject.ContextScope;
import roboguice.inject.ContextSingleton;

@ContextSingleton
class GitHubSyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject
    private ContextScope contextScope;

    @Inject
    private GitHubAccountScope gitHubAccountScope;

    @Inject
    private SyncCampaign.Factory syncCampaignFactory;

    private SyncCampaign currentSyncCampaign = null;

    @Inject
    public GitHubSyncAdapter(Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        gitHubAccountScope.enterWith(account, AccountManager.get(getContext()));
        try {
            contextScope.enter(getContext());
            try {
                cancelAnyCurrentCampaign();
                currentSyncCampaign = syncCampaignFactory.createCampaignFor(syncResult);
                currentSyncCampaign.run();
            } finally {
                contextScope.exit(getContext());
            }
        } finally {
            gitHubAccountScope.exit();
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
