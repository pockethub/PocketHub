/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.github.pockethub.accounts.AccountScope;
import com.github.pockethub.sync.SyncCampaign.Factory;
import com.google.inject.Inject;

import roboguice.inject.ContextScope;
import roboguice.inject.ContextSingleton;

/**
 * Sync adapter
 */
@ContextSingleton
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject
    private ContextScope contextScope;

    @Inject
    private AccountScope accountScope;

    @Inject
    private Factory campaignFactory;

    private SyncCampaign campaign = null;

    /**
     * Create sync adapter for context
     *
     * @param context
     */
    @Inject
    public SyncAdapter(final Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(final Account account, final Bundle extras,
            final String authority, final ContentProviderClient provider,
            final SyncResult syncResult) {
        accountScope.enterWith(account, AccountManager.get(getContext()));
        try {
            contextScope.enter(getContext());
            try {
                cancelCampaign();
                campaign = campaignFactory.create(syncResult);
                campaign.run();
            } finally {
                contextScope.exit(getContext());
            }
        } finally {
            accountScope.exit();
        }
    }

    @Override
    public void onSyncCanceled() {
        cancelCampaign();
    }

    private void cancelCampaign() {
        if (campaign != null)
            campaign.cancel();
    }
}
