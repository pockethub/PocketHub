/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.github.mobile.guice.GitHubAccountScope;
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
