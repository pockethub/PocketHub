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

import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static com.github.mobile.accounts.AccountConstants.ACCOUNT_TYPE;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.OutOfScopeException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom scope that makes an authenticated GitHub account available by
 * enforcing that the user is logged in before proceeding.
 */
public class AccountScope extends ScopeBase {

    private static final Key<GitHubAccount> GITHUB_ACCOUNT_KEY = Key
            .get(GitHubAccount.class);

    private static final String TAG = "GitHubAccountScope";

    /**
     * Create new module
     *
     * @return module
     */
    public static Module module() {
        return new AbstractModule() {
            public void configure() {
                AccountScope scope = new AccountScope();

                bind(AccountScope.class).toInstance(scope);

                bind(GITHUB_ACCOUNT_KEY).toProvider(
                        AccountScope.<GitHubAccount> seededKeyProvider()).in(
                        scope);
            }
        };
    }

    private final ThreadLocal<GitHubAccount> currentAccount = new ThreadLocal<GitHubAccount>();

    private final Map<GitHubAccount, Map<Key<?>, Object>> repoScopeMaps = new ConcurrentHashMap<GitHubAccount, Map<Key<?>, Object>>();

    /**
     * Enters scope using a GitHubAccount derived from the supplied account
     *
     * @param account
     * @param accountManager
     */
    public void enterWith(final Account account,
            final AccountManager accountManager) {
        @SuppressWarnings("deprecation")
        AccountManagerFuture<Bundle> future = accountManager.getAuthToken(
                account, ACCOUNT_TYPE, false, null, null);

        String authToken;
        try {
            authToken = future.getResult().getString(KEY_AUTHTOKEN);
        } catch (AuthenticatorException ae) {
            authToken = null;
            // Authenticator failed to respond
            Log.e(TAG, ae.getMessage());
        } catch (OperationCanceledException oce) {
            authToken = null;
            // User canceled operation
            Log.e(TAG, oce.getMessage());
        } catch (IOException ioe) {
            authToken = null;
            // Possible network issues
            Log.e(TAG, ioe.getMessage());
        }

        String password;
        if (TextUtils.isEmpty(authToken))
            password = accountManager.getPassword(account);
        else
            password = null;
        enterWith(new GitHubAccount(account.name, password, authToken));
    }

    /**
     * Enter scope with account
     *
     * @param account
     */
    public void enterWith(final GitHubAccount account) {
        if (currentAccount.get() != null)
            throw new IllegalStateException(
                    "A scoping block is already in progress");

        currentAccount.set(account);
    }

    /**
     * Exit scope
     */
    public void exit() {
        if (currentAccount.get() == null)
            throw new IllegalStateException("No scoping block in progress");

        currentAccount.remove();
    }

    @Override
    protected <T> Map<Key<?>, Object> getScopedObjectMap(final Key<T> key) {
        GitHubAccount account = currentAccount.get();
        if (account == null)
            throw new OutOfScopeException("Cannot access " + key
                    + " outside of a scoping block");

        Map<Key<?>, Object> scopeMap = repoScopeMaps.get(account);
        if (scopeMap == null) {
            scopeMap = new ConcurrentHashMap<Key<?>, Object>();
            scopeMap.put(GITHUB_ACCOUNT_KEY, account);
            repoScopeMaps.put(account, scopeMap);
        }
        return scopeMap;
    }
}
