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
package com.github.pockethub.android.sync;

import android.content.SyncResult;
import android.database.SQLException;
import android.util.Log;
import com.github.pockethub.android.persistence.DatabaseCache;
import com.github.pockethub.android.persistence.OrganizationRepositoriesFactory;
import com.github.pockethub.android.persistence.Organizations;
import com.google.auto.factory.AutoFactory;
import com.meisolsson.githubsdk.model.User;

import javax.inject.Inject;
import java.util.List;

/**
 * A cancelable sync operation to synchronize data for a given account
 */
@AutoFactory
public class SyncCampaign implements Runnable {

    private static final String TAG = "SyncCampaign";

    @Inject
    protected DatabaseCache cache;

    @Inject
    protected OrganizationRepositoriesFactory repos;

    @Inject
    protected Organizations persistedOrgs;

    private final SyncResult syncResult;

    private boolean cancelled = false;

    /**
     * Create campaign for result
     *
     * @param syncResult
     */
    public SyncCampaign(SyncResult syncResult) {
        this.syncResult = syncResult;
    }

    @Override
    public void run() {
        List<User> orgs;
        try {
            orgs = cache.requestAndStore(persistedOrgs);
            syncResult.stats.numUpdates++;
        } catch (SQLException e) {
            syncResult.stats.numIoExceptions++;
            Log.d(TAG, "Exception requesting users and orgs", e);
            return;
        }

        Log.d(TAG, "Syncing " + orgs.size() + " users and orgs");
        for (User org : orgs) {
            if (cancelled) {
                return;
            }

            Log.d(TAG, "Syncing repos for " + org.login());
            try {
                cache.requestAndStore(repos.create(org));
                syncResult.stats.numUpdates++;
            } catch (SQLException e) {
                syncResult.stats.numIoExceptions++;
                Log.d(TAG, "Exception requesting repositories", e);
            }
        }

        Log.d(TAG, "Sync campaign finished");
    }

    /**
     * Cancel campaign
     */
    public void cancel() {
        cancelled = true;
        Log.d(TAG, "Cancelled");
    }
}
