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
package com.github.pockethub.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.github.pockethub.android.core.commit.CommitStore;
import com.github.pockethub.android.core.gist.GistStore;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.persistence.OrganizationRepositories;
import com.github.pockethub.android.sync.SyncCampaign;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;


import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Main module provide services and clients
 */
public class PocketHubModule extends AbstractModule {

    private WeakReference<IssueStore> issues;

    private WeakReference<GistStore> gists;

    private WeakReference<CommitStore> commits;

    @Override
    protected void configure() {
        install(new ServicesModule());
        install(new FactoryModuleBuilder().build(SyncCampaign.Factory.class));
        install(new FactoryModuleBuilder()
                .build(OrganizationRepositories.Factory.class));
    }

    @Provides
    Account account(Context context){
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByType(context.getString(R.string.account_type));
        return accounts[0];
    }

    @Provides
    @Named("cacheDir")
    File cacheDir(Context context) {
        return new File(context.getFilesDir(), "cache");
    }

    @Provides
    IssueStore issueStore(Context context) {
        IssueStore store = issues != null ? issues.get() : null;
        if (store == null) {
            store = new IssueStore(context);
            issues = new WeakReference<>(store);
        }
        return store;
    }

    @Provides
    GistStore gistStore(Context context) {
        GistStore store = gists != null ? gists.get() : null;
        if (store == null) {
            store = new GistStore(context);
            gists = new WeakReference<>(store);
        }
        return store;
    }

    @Provides
    CommitStore commitStore(Context context) {
        CommitStore store = commits != null ? commits.get() : null;
        if (store == null) {
            store = new CommitStore(context);
            commits = new WeakReference<>(store);
        }
        return store;
    }
}
