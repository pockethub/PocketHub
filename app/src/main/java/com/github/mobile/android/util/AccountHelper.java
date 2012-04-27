package com.github.mobile.android.util;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.util.Log.*;
import static com.github.mobile.android.authenticator.Constants.GITHUB_ACCOUNT_TYPE;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
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

    /**
     * Get account used for authentication
     *
     * @param accountManager
     * @param activityUsedToStartLoginProcess
     * @return account
     */
    public static Account getAccount(final AccountManager accountManager, final Activity activityUsedToStartLoginProcess) {
        final boolean loggable = Log.isLoggable(TAG, DEBUG);
        if (loggable)
            Log.d(TAG, "Getting current account...");

        Account[] accounts;
        try {
            while ((accounts = accountManager.getAccountsByTypeAndFeatures(GITHUB_ACCOUNT_TYPE, null, null, null)
                    .getResult()).length == 0) {

                if (loggable)
                    Log.d(TAG, "No GitHub accounts for activity=" + activityUsedToStartLoginProcess);

                if (activityUsedToStartLoginProcess == null)
                    throw new RuntimeException("Can't create new GitHub account - no activity available");

                Bundle result = accountManager.addAccount(GITHUB_ACCOUNT_TYPE, null, null, null,
                        activityUsedToStartLoginProcess, null, null).getResult();

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
