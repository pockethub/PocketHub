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
package com.github.mobile.accounts;

import static com.github.mobile.accounts.AccountUtils.getAccount;
import static com.google.common.base.Preconditions.checkState;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.util.Log;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.OutOfScopeException;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;


/**
 * Custom Guice-scope that makes an authenticated GitHub account available,
 * by enforcing that the user is logged in before proceeding.
 */
public class GitHubAccountScope extends ScopeBase {

    private final static String TAG = "GitHubAccountScope";

    private final static Key<GitHubAccount> GITHUB_ACCOUNT_KEY = Key.get(GitHubAccount.class);

    public static Module module() {
        return new AbstractModule() {
            public void configure() {
                GitHubAccountScope scope = new GitHubAccountScope();

                bind(GitHubAccountScope.class).toInstance(scope);

                bind(GITHUB_ACCOUNT_KEY).toProvider(GitHubAccountScope.<GitHubAccount>seededKeyProvider()).in(scope);
            }
        };
    }

    private final ThreadLocal<GitHubAccount> currentAccount = new ThreadLocal<GitHubAccount>();

    private final Map<GitHubAccount, Map<Key<?>, Object>> repoScopeMaps = new MapMaker().
        makeComputingMap(new Function<GitHubAccount, Map<Key<?>, Object>>() {
            public Map<Key<?>, Object> apply(GitHubAccount account) {
                ConcurrentMap<Key<?>, Object> accountScopeMap = new MapMaker().makeMap();
                accountScopeMap.put(GITHUB_ACCOUNT_KEY, account);
                return accountScopeMap;
            }
        });

    /**
     * Enters scope once we've ensured the user has a valid account.
     */
    public void enterWith(Activity activityUsedToStartLoginProcess) {
        AccountManager accountManager = AccountManager.get(activityUsedToStartLoginProcess);
        Account account = getAccount(accountManager, activityUsedToStartLoginProcess);
        enterWith(account, accountManager);
    }

    /**
     * Enters scope using a GitHubAccount derived from the supplied account
     */
    public void enterWith(Account account, AccountManager accountManager) {
        enterWith(new GitHubAccount(account.name, accountManager.getPassword(account)));
    }

    public void enterWith(GitHubAccount account) {
        Log.d(TAG, "entering scope with " + account);
        checkState(currentAccount.get() == null, "A scoping block is already in progress");
        currentAccount.set(account);
    }

    public void exit() {
        Log.d(TAG, "exiting scope");
        checkState(currentAccount.get() != null, "No scoping block in progress");
        currentAccount.remove();
    }

    @Override
    protected <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key) {
        GitHubAccount account = currentAccount.get();
        if (account == null) {
            throw new OutOfScopeException("Cannot access " + key + " outside of a scoping block");
        }
        return repoScopeMaps.get(account);
    }


}