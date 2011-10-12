package com.github.android.app.sync;

import android.accounts.*;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import static android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT;
import static android.content.ContentResolver.addPeriodicSync;
import static android.content.ContentResolver.setIsSyncable;
import static android.content.ContentResolver.setSyncAutomatically;
import static com.github.android.app.sync.Constants.*;

/**
 * Authenticator service that returns a subclass of AbstractAccountAuthenticator in onBind()
 */
public class AccountAuthenticatorService extends Service {
    private static final String TAG = "AccountAuthenticatorService";
    private static AccountAuthenticatorImpl sAccountAuthenticator = null;

    public AccountAuthenticatorService() {
        super();
    }

    public IBinder onBind(Intent intent) {
		return intent.getAction().equals(ACTION_AUTHENTICATOR_INTENT)?getAuthenticator().getIBinder():null;
    }

    private AccountAuthenticatorImpl getAuthenticator() {
        if (sAccountAuthenticator == null)
            sAccountAuthenticator = new AccountAuthenticatorImpl(this);
        return sAccountAuthenticator;
    }

    public static Bundle addAccount(Context ctx) {
        Bundle result = null;
        Account account = new Account(GITHUB_ACCOUNT_NAME, GITHUB_ACCOUNT_TYPE);
        AccountManager am = AccountManager.get(ctx);
        if (am.addAccountExplicitly(account, null, null)) {
            result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        }
        configureSyncFor(account);
        return result;
    }

    private static void configureSyncFor(Account account) {
        Log.d(TAG, "Trying to configure account for sync...");
        setIsSyncable(account, GITHUB_PROVIDER_AUTHORITY, 1);
        setSyncAutomatically(account, GITHUB_PROVIDER_AUTHORITY, true);
		addPeriodicSync(account, GITHUB_PROVIDER_AUTHORITY, new Bundle(), (long) (15 * 60));
	}

	private static class AccountAuthenticatorImpl extends AbstractAccountAuthenticator {
        private Context mContext;

        public AccountAuthenticatorImpl(Context context) {
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
            Log.d(TAG,"addAccount "+accountType+" authTokenType="+authTokenType);
            return null;
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
}