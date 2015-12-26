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
package com.github.pockethub.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static com.github.pockethub.accounts.AccountConstants.ACCOUNT_TYPE;

/**
 * GitHub account model
 */
public class GitHubAccount {

    private static final String TAG = "GitHubAccount";

    private final Account account;

    private final AccountManager manager;

    /**
     * Create account wrapper
     *
     * @param account
     * @param manager
     */
    public GitHubAccount(final Account account, final AccountManager manager) {
        this.account = account;
        this.manager = manager;
    }

    /**
     * Get username
     *
     * @return username
     */
    public String getUsername() {
        return account.name;
    }

    /**
     * Get password
     *
     * @return password
     */
    public String getPassword() {
        return manager.getPassword(account);
    }

    /**
     * Get auth token
     *
     * @return token
     */
    public String getAuthToken() {
        AccountManagerFuture<Bundle> future = manager.getAuthToken(account,
                ACCOUNT_TYPE, false, null, null);

        try {
            Bundle result = future.getResult();
            return result != null ? result.getString(KEY_AUTHTOKEN) : null;
        } catch (AccountsException e) {
            Log.e(TAG, "Auth token lookup failed", e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Auth token lookup failed", e);
            return null;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + account.name + ']';
    }
}
