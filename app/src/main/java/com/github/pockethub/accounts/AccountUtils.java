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
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alorma.github.sdk.bean.dto.response.Organization;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.R;

import org.eclipse.egit.github.core.client.RequestException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.util.Log.DEBUG;
import static com.github.pockethub.accounts.AccountConstants.ACCOUNT_TYPE;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * Helpers for accessing {@link AccountManager}
 */
public class AccountUtils {

    private static final String TAG = "AccountUtils";

    private static boolean AUTHENTICATOR_CHECKED;

    private static boolean HAS_AUTHENTICATOR;

    private static final AtomicInteger UPDATE_COUNT = new AtomicInteger(0);

    private static class AuthenticatorConflictException extends IOException {

        private static final long serialVersionUID = 641279204734869183L;
    }

    /**
     * Verify authenticator registered for account type matches the package name
     * of this application
     *
     * @param manager
     * @return true is authenticator registered, false otherwise
     */
    public static boolean hasAuthenticator(final AccountManager manager) {
        if (!AUTHENTICATOR_CHECKED) {
            final AuthenticatorDescription[] types = manager
                    .getAuthenticatorTypes();
            if (types != null && types.length > 0)
                for (AuthenticatorDescription descriptor : types)
                    if (descriptor != null
                            && ACCOUNT_TYPE.equals(descriptor.type)) {
                        HAS_AUTHENTICATOR = "com.github.pockethub"
                                .equals(descriptor.packageName);
                        break;
                    }
            AUTHENTICATOR_CHECKED = true;
        }

        return HAS_AUTHENTICATOR;
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

        String login = user.login;
        if (login == null)
            return false;

        return login.equals(getLogin(context));
    }

    /**
     * Is the given user the owner of the default account?
     *
     * @param context
     * @param user
     * @return true if default account user, false otherwise
     */
    public static boolean isUser(final Context context, final Organization user) {
        if (user == null)
            return false;

        String login = user.login;
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

    /**
     * Get default account where password can be retrieved
     *
     * @param context
     * @return password accessible account or null if none
     */
    public static Account getPasswordAccessibleAccount(final Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts == null || accounts.length == 0)
            return null;

        try {
            accounts = getPasswordAccessibleAccounts(manager, accounts);
        } catch (AuthenticatorConflictException e) {
            return null;
        }
        return accounts != null && accounts.length > 0 ? accounts[0] : null;
    }

    private static Account[] getPasswordAccessibleAccounts(
            final AccountManager manager, final Account[] candidates)
            throws AuthenticatorConflictException {
        final List<Account> accessible = new ArrayList<>(
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

        if (activity.isFinishing())
            throw new OperationCanceledException();

        Account[] accounts;
        try {
            if (!hasAuthenticator(manager))
                throw new AuthenticatorConflictException();

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
     * Update account
     *
     * @param account
     * @param activity
     * @return true if account was updated, false otherwise
     */
    public static boolean updateAccount(final Account account,
            final Activity activity) {
        int count = UPDATE_COUNT.get();
        synchronized (UPDATE_COUNT) {
            // Don't update the account if the account was successfully updated
            // while the lock was being waited for
            if (count != UPDATE_COUNT.get())
                return true;

            AccountManager manager = AccountManager.get(activity);
            try {
                if (!hasAuthenticator(manager))
                    throw new AuthenticatorConflictException();
                manager.updateCredentials(account, ACCOUNT_TYPE, null,
                        activity, null, null).getResult();
                UPDATE_COUNT.incrementAndGet();
                return true;
            } catch (OperationCanceledException e) {
                Log.d(TAG, "Excepting retrieving account", e);
                activity.finish();
                return false;
            } catch (AccountsException e) {
                Log.d(TAG, "Excepting retrieving account", e);
                return false;
            } catch (AuthenticatorConflictException e) {
                activity.runOnUiThread(new Runnable() {

                    public void run() {
                        showConflictMessage(activity);
                    }
                });
                return false;
            } catch (IOException e) {
                Log.d(TAG, "Excepting retrieving account", e);
                return false;
            }
        }
    }

    /**
     * Show conflict message about previously registered authenticator from
     * another application
     *
     * @param activity
     */
    private static void showConflictMessage(final Activity activity) {
        new MaterialDialog.Builder(activity)
                .title(R.string.authenticator_conflict_title)
                .content(R.string.authenticator_conflict_message)
                .positiveText(android.R.string.ok)
                .cancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        activity.finish();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        activity.finish();
                    }
                })
                .show();
    }

    /**
     * Is the given {@link Exception} due to a 401 Unauthorized API response?
     *
     * @param e
     * @return true if 401, false otherwise
     */
    public static boolean isUnauthorized(final Exception e) {
        if (e instanceof RequestException)
            return ((RequestException) e).getStatus() == HTTP_UNAUTHORIZED;

        String message = null;
        if (e instanceof IOException)
            message = e.getMessage();
        final Throwable cause = e.getCause();
        if (cause instanceof IOException) {
            String causeMessage = cause.getMessage();
            if (!TextUtils.isEmpty(causeMessage))
                message = causeMessage;
        }

        if (TextUtils.isEmpty(message))
            return false;

        if ("Received authentication challenge is null".equals(message))
            return true;
        return "No authentication challenges found".equals(message);

    }
}
