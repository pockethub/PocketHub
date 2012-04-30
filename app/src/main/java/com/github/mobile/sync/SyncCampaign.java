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

import android.content.SyncResult;
import android.util.Log;

import com.github.mobile.persistence.OrganizationRepositories;
import com.github.mobile.persistence.DatabaseCache;
import com.github.mobile.persistence.Organizations;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.User;

/**
 * A cancellable Sync operation - aims to synchronize data
 * for a given account.
 */
public class SyncCampaign implements Runnable {
    private static final String TAG = "SyncCampaign";

    @Inject
    private DatabaseCache dbCache;

    @Inject
    private OrganizationRepositories.Factory allRepos;

    @Inject
    private Organizations userAndOrgsResource;

    private final SyncResult syncResult;
    private boolean cancelled = false;

    @Inject
    public SyncCampaign(@Assisted SyncResult syncResult) {
        this.syncResult = syncResult;
    }

    public void run() {
        List<User> usersAndOrgs;
        try {
            usersAndOrgs = dbCache.requestAndStore(userAndOrgsResource);
            syncResult.stats.numUpdates++;
        } catch (IOException e) {
            syncResult.stats.numIoExceptions++;
            Log.d(TAG, "Exception requesting users & orgs", e);
            return;
        }

        Log.d(TAG, "Found " + usersAndOrgs.size() + " users and orgs for sync");
        for (User userOrOrg : usersAndOrgs) {
            if (cancelled)
                return;

            Log.d(TAG, "Syncing repos for " + userOrOrg.getLogin() + "...");
            try {
                dbCache.requestAndStore(allRepos.under(userOrOrg));
                syncResult.stats.numUpdates++;
            } catch (IOException e) {
                syncResult.stats.numIoExceptions++;
                Log.d(TAG, "Exception requesting repositories", e);
            }
        }
        Log.d(TAG, "...finished sync campaign");
    }

    public void cancel() {
        cancelled = true;
        Log.d(TAG, "Cancelled");
    }

    public interface Factory {
        public SyncCampaign createCampaignFor(SyncResult syncResult);
    }
}
