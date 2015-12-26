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

import android.content.SyncResult;
import android.database.SQLException;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Organization;
import com.github.pockethub.persistence.DatabaseCache;
import com.github.pockethub.persistence.OrganizationRepositories;
import com.github.pockethub.persistence.Organizations;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.io.IOException;
import java.util.List;

/**
 * A cancelable sync operation to synchronize data for a given account
 */
public class SyncCampaign implements Runnable {

    private static final String TAG = "SyncCampaign";

    /**
     * Factory to create campaign
     */
    public interface Factory {

        /**
         * Create campaign for result
         *
         * @param syncResult
         * @return campaign
         */
        SyncCampaign create(SyncResult syncResult);
    }

    @Inject
    private DatabaseCache cache;

    @Inject
    private OrganizationRepositories.Factory repos;

    @Inject
    private Organizations persistedOrgs;

    private final SyncResult syncResult;

    private boolean cancelled = false;

    /**
     * Create campaign for result
     *
     * @param syncResult
     */
    @Inject
    public SyncCampaign(@Assisted SyncResult syncResult) {
        this.syncResult = syncResult;
    }

    @Override
    public void run() {
        List<Organization> orgs;
        try {
            orgs = cache.requestAndStore(persistedOrgs);
            syncResult.stats.numUpdates++;
        } catch (IOException | SQLException e) {
            syncResult.stats.numIoExceptions++;
            Log.d(TAG, "Exception requesting users and orgs", e);
            return;
        }

        Log.d(TAG, "Syncing " + orgs.size() + " users and orgs");
        for (Organization org : orgs) {
            if (cancelled)
                return;

            Log.d(TAG, "Syncing repos for " + org.login);
            try {
                cache.requestAndStore(repos.under(org));
                syncResult.stats.numUpdates++;
            } catch (IOException | SQLException e) {
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
