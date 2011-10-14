package com.github.mobile.android.authenticator;

import android.accounts.*;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import static android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT;
import static android.content.ContentResolver.addPeriodicSync;
import static android.content.ContentResolver.setIsSyncable;
import static android.content.ContentResolver.setSyncAutomatically;
import static com.github.mobile.android.authenticator.Constants.*;

/**
 * Authenticator service that returns a subclass of AbstractAccountAuthenticator in onBind()
 */
public class AccountAuthenticatorService extends Service {
    private static final String TAG = "AccountAuthenticatorService";
    private static GitHubAccountAuthenticator sAccountAuthenticator = null;

    public AccountAuthenticatorService() {
        super();
    }

    public IBinder onBind(Intent intent) {
		return intent.getAction().equals(ACTION_AUTHENTICATOR_INTENT)?getAuthenticator().getIBinder():null;
    }

    private GitHubAccountAuthenticator getAuthenticator() {
        if (sAccountAuthenticator == null)
            sAccountAuthenticator = new GitHubAccountAuthenticator(this);
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

}