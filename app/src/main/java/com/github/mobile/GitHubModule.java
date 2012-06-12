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
package com.github.mobile;

import android.content.Context;

import com.github.mobile.accounts.AccountClient;
import com.github.mobile.accounts.AccountScope;
import com.github.mobile.accounts.GitHubAccount;
import com.github.mobile.core.gist.GistStore;
import com.github.mobile.core.issue.IssueStore;
import com.github.mobile.persistence.OrganizationRepositories;
import com.github.mobile.sync.SyncCampaign;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;

import java.io.File;
import java.lang.ref.WeakReference;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Main module provide services and clients
 */
public class GitHubModule extends AbstractModule {

    private WeakReference<IssueStore> issues;

    private WeakReference<GistStore> gists;

    @Override
    protected void configure() {
        install(new ServicesModule());
        install(new FactoryModuleBuilder().build(SyncCampaign.Factory.class));
        install(new FactoryModuleBuilder()
                .build(OrganizationRepositories.Factory.class));
        install(AccountScope.module());
    }

    @Provides
    GitHubClient client(Provider<GitHubAccount> accountProvider) {
        return new AccountClient(accountProvider);
    }

    @Provides
    @Named("cacheDir")
    File cacheDir(Context context) {
        return new File(context.getFilesDir(), "cache");
    }

    @Provides
    IssueStore issueStore(IssueService service) {
        IssueStore store = issues != null ? issues.get() : null;
        if (store == null) {
            store = new IssueStore(service);
            issues = new WeakReference<IssueStore>(store);
        }
        return store;
    }

    @Provides
    GistStore gistStore(GistService service) {
        GistStore store = gists != null ? gists.get() : null;
        if (store == null) {
            store = new GistStore(service);
            gists = new WeakReference<GistStore>(store);
        }
        return store;
    }
}
