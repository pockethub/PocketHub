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

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.util.Log.DEBUG;
import static com.github.mobile.accounts.Constants.GITHUB_ACCOUNT_TYPE;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

/**
 * Helpers for accessing {@link AccountManager}
 */
public class AccountUtils {

    private static final String TAG = "GH.AccountHelper";

    /**
     * Get login name of configured account
     *
     * @param context
     * @return login name or null if none configure
     */
    public static String getLogin(final Context context) {
        final Account[] accounts = AccountManager.get(context).getAccountsByType(GITHUB_ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0].name : null;
    }

    private static Account[] getAccounts(final AccountManager manager) throws OperationCanceledException,
            AuthenticatorException, IOException {
        final AccountManagerFuture<Account[]> future = manager.getAccountsByTypeAndFeatures(GITHUB_ACCOUNT_TYPE, null,
                null, null);
        final Account[] accounts = future.getResult();
        return accounts != null ? accounts : new Account[0];
    }

    /**
     * Get account used for authentication
     *
     * @param manager
     * @param activity
     * @return account
     */
    public static Account getAccount(final AccountManager manager, final Activity activity) {
        final boolean loggable = Log.isLoggable(TAG, DEBUG);
        if (loggable)
            Log.d(TAG, "Getting account");

        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null");

        Account[] accounts;
        try {
            while ((accounts = getAccounts(manager)).length == 0) {
                if (loggable)
                    Log.d(TAG, "No GitHub accounts for activity=" + activity);

                Bundle result = manager.addAccount(GITHUB_ACCOUNT_TYPE, null, null, null, activity, null, null)
                        .getResult();

                if (loggable)
                    Log.d(TAG, "Added account " + result.getString(KEY_ACCOUNT_NAME));
            }
        } catch (AuthenticatorException e) {
            Log.d(TAG, "Excepting retrieving account", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            Log.d(TAG, "Excepting retrieving account", e);
            throw new RuntimeException(e);
        } catch (OperationCanceledException e) {
            Log.d(TAG, "Excepting retrieving account", e);
            throw new RuntimeException(e);
        }

        if (loggable)
            Log.d(TAG, "Returning account " + accounts[0].name);

        return accounts[0];
    }
}
