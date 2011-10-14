package com.github.android.app.authenticator;

import android.accounts.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import static com.github.android.app.authenticator.Constants.AUTHTOKEN_TYPE;

class GitHubAccountAuthenticator extends AbstractAccountAuthenticator {
    private Context mContext;

    public GitHubAccountAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    /*
    *  The user has requested to add a new account to the system.  We return an intent that will launch our login screen if the user has not logged in yet,
    *  otherwise our activity will just pass the user's credentials on to the account manager.
    */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
            final Intent intent = new Intent(mContext, GitHubAuthenticatorActivity.class);
            intent.putExtra(GitHubAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            final Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
// Log.d(AccountAuthenticatorService.TAG, "addAccount " + accountType + " authTokenType=" + authTokenType);
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (authTokenType.equals(AUTHTOKEN_TYPE)) {
            return "FooBooHurrah";
        }
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        return null;
    }
}
