package com.github.mobile.android.util;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static com.github.mobile.android.authenticator.Constants.GITHUB_ACCOUNT_TYPE;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * Helpers for accessing {@link AccountManager}
 */
public class AccountHelper {

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

    public static Account demandCurrentAccount(AccountManager accountManager,
        Activity activityUsedToStartLoginProcess) {
        Account[] accounts;
        Log.d(TAG, "Getting current account...");
        try {
            while ((accounts = accountManager.
                getAccountsByTypeAndFeatures(GITHUB_ACCOUNT_TYPE, null, null, null).getResult()).length == 0) {
                Log.d(TAG, "Currently zero GitHub accounts... activity=" + activityUsedToStartLoginProcess);
                if (activityUsedToStartLoginProcess == null)
                    throw new RuntimeException("Can't create new GitHub account - no activity available");

                Bundle result = accountManager.addAccount(GITHUB_ACCOUNT_TYPE, null, null, null,
                    activityUsedToStartLoginProcess, null, null).getResult();

                Log.i(TAG, "Added account " + result.getString(KEY_ACCOUNT_NAME));
            }
        } catch (Exception e) {
            Log.e(TAG, "Problem getting a Github account...", e);
            throw new RuntimeException(e);
        }

        Account account = accounts[0];
        Log.d(TAG, "Returning account " + account.name);
        return account;
    }
}
