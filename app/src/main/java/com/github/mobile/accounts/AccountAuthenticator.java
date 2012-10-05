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

import static android.accounts.AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE;
import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static android.accounts.AccountManager.KEY_INTENT;
import static com.github.mobile.accounts.AccountConstants.ACCOUNT_TYPE;
import static com.github.mobile.accounts.AccountConstants.APP_NOTE;
import static com.github.mobile.accounts.AccountConstants.APP_NOTE_URL;
import static com.github.mobile.accounts.LoginActivity.PARAM_AUTHTOKEN_TYPE;
import static com.github.mobile.accounts.LoginActivity.PARAM_USERNAME;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.github.mobile.DefaultClient;

import java.util.Arrays;
import java.util.List;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.service.OAuthService;

class AccountAuthenticator extends AbstractAccountAuthenticator {

    private static final String TAG = "GitHubAccountAuthenticator";

    private Context context;

    public AccountAuthenticator(final Context context) {
        super(context);

        this.context = context;
    }

    /**
     * The user has requested to add a new account to the system. We return an
     * intent that will launch our login screen if the user has not logged in
     * yet, otherwise our activity will just pass the user's credentials on to
     * the account manager.
     */
    @Override
    public Bundle addAccount(final AccountAuthenticatorResponse response,
            final String accountType, final String authTokenType,
            String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
            Account account, Bundle options) {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
            String accountType) {
        return null;
    }

    private boolean isValidAuthorization(final Authorization auth,
            final List<String> requiredScopes) {
        if (auth == null)
            return false;

        if (!APP_NOTE.equals(auth.getNote()))
            return false;

        if (!APP_NOTE_URL.equals(auth.getNoteUrl()))
            return false;

        List<String> scopes = auth.getScopes();
        if (scopes == null || scopes.size() != requiredScopes.size())
            return false;

        for (String required : requiredScopes)
            if (!scopes.contains(required))
                return false;
        return true;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {

        final Bundle bundle = new Bundle();

        if (!ACCOUNT_TYPE.equals(authTokenType))
            return bundle;

        AccountManager am = AccountManager.get(context);
        String username = account.name;
        String password = am.getPassword(account);

        String authToken = null;
        DefaultClient client = new DefaultClient();
        client.setCredentials(username, password);
        OAuthService oAuthService = new OAuthService(client);

        List<String> requiredScopes = Arrays.asList("repo", "user", "gist");

        // Get authorizations for app if they exist
        try {
            List<Authorization> auths = oAuthService.getAuthorizations();
            for (Authorization auth : auths)
                if (isValidAuthorization(auth, requiredScopes)) {
                    authToken = auth.getToken();
                    break;
                }

            // Setup authorization for app if others didn't exist.
            if (TextUtils.isEmpty(authToken)) {
                Authorization auth = new Authorization();
                auth.setNote(APP_NOTE);
                auth.setNoteUrl(APP_NOTE_URL);
                auth.setScopes(requiredScopes);
                auth = oAuthService.createAuthorization(auth);
                if (auth != null)
                    authToken = auth.getToken();
            }

            // If couldn't get authToken
            if (TextUtils.isEmpty(authToken)) {
                final Intent intent = new Intent(context, LoginActivity.class);
                intent.putExtra(PARAM_AUTHTOKEN_TYPE, ACCOUNT_TYPE);
                intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
                bundle.putParcelable(KEY_INTENT, intent);
                return bundle;
            }

            // Assemble and return bundle
            bundle.putString(KEY_ACCOUNT_NAME, account.name);
            bundle.putString(KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
            bundle.putString(KEY_AUTHTOKEN, authToken);

            // Clear password from account
            am.clearPassword(account);
            return bundle;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (ACCOUNT_TYPE.equals(authTokenType))
            return authTokenType;
        else
            return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
            Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options) {
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        if (!TextUtils.isEmpty(account.name))
            intent.putExtra(PARAM_USERNAME, account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INTENT, intent);
        return bundle;
    }
}
