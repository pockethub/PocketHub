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
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.util.Log.DEBUG;
import static com.github.mobile.accounts.AccountConstants.ACCOUNT_TYPE;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;

import com.github.mobile.R.string;
import com.github.mobile.ui.LightAlertDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.User;

/**
 * Helpers for accessing {@link AccountManager}
 */
public class AccountUtils {

    private static final String TAG = "AccountUtils";

    private static class AuthenticatorConflictException extends IOException {

        private static final long serialVersionUID = 641279204734869183L;
    }

    /**
     * Is the given user the owner of the default account?
     *
     * @param context
     * @param user
     * @return true if default account user, false otherwise
     */
    public static boolean isUser(final Context context, final User user) {
        if (user == null)
            return false;

        String login = user.getLogin();
        if (login == null)
            return false;

        return login.equals(getLogin(context));
    }

    /**
     * Get login name of configured account
     *
     * @param context
     * @return login name or null if none configure
     */
    public static String getLogin(final Context context) {
        final Account account = getAccount(context);
        return account != null ? account.name : null;
    }

    /**
     * Get configured account
     *
     * @param context
     * @return account or null if none
     */
    public static Account getAccount(final Context context) {
        final Account[] accounts = AccountManager.get(context)
                .getAccountsByType(ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0] : null;
    }

    private static Account[] getAccounts(final AccountManager manager)
            throws OperationCanceledException, AuthenticatorException,
            IOException {
        final AccountManagerFuture<Account[]> future = manager
                .getAccountsByTypeAndFeatures(ACCOUNT_TYPE, null, null, null);
        final Account[] accounts = future.getResult();
        if (accounts != null && accounts.length > 0)
            return getPasswordAccessibleAccounts(manager, accounts);
        else
            return new Account[0];
    }

    private static Account[] getPasswordAccessibleAccounts(
            final AccountManager manager, final Account[] candidates)
            throws AuthenticatorConflictException {
        final List<Account> accessible = new ArrayList<Account>(
                candidates.length);
        boolean exceptionThrown = false;
        for (Account account : candidates)
            try {
                manager.getPassword(account);
                accessible.add(account);
            } catch (SecurityException ignored) {
                exceptionThrown = true;
            }
        if (accessible.isEmpty() && exceptionThrown)
            throw new AuthenticatorConflictException();
        return accessible.toArray(new Account[accessible.size()]);
    }

    /**
     * Get account used for authentication
     *
     * @param manager
     * @param activity
     * @return account
     * @throws IOException
     * @throws AccountsException
     */
    public static Account getAccount(final AccountManager manager,
            final Activity activity) throws IOException, AccountsException {
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

                Bundle result = manager.addAccount(ACCOUNT_TYPE, null, null,
                        null, activity, null, null).getResult();

                if (loggable)
                    Log.d(TAG,
                            "Added account "
                                    + result.getString(KEY_ACCOUNT_NAME));
            }
        } catch (OperationCanceledException e) {
            Log.d(TAG, "Excepting retrieving account", e);
            activity.finish();
            throw e;
        } catch (AccountsException e) {
            Log.d(TAG, "Excepting retrieving account", e);
            throw e;
        } catch (AuthenticatorConflictException e) {
            activity.runOnUiThread(new Runnable() {

                public void run() {
                    showConflictMessage(activity);
                }
            });
            throw e;
        } catch (IOException e) {
            Log.d(TAG, "Excepting retrieving account", e);
            throw e;
        }

        if (loggable)
            Log.d(TAG, "Returning account " + accounts[0].name);

        return accounts[0];
    }

    /**
     * Show conflict message about previously registered authenticator from
     * another application
     *
     * @param activity
     */
    private static void showConflictMessage(final Activity activity) {
        AlertDialog dialog = LightAlertDialog.create(activity);
        dialog.setTitle(activity.getString(string.authenticator_conflict_title));
        dialog.setMessage(activity
                .getString(string.authenticator_conflict_message));
        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                activity.finish();
            }
        });
        dialog.setButton(BUTTON_POSITIVE,
                activity.getString(android.R.string.ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                });
        dialog.show();
    }
}
