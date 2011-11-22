package com.github.mobile.android.authenticator;

import static android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT;
import static android.content.ContentResolver.addPeriodicSync;
import static android.content.ContentResolver.setIsSyncable;
import static android.content.ContentResolver.setSyncAutomatically;
import static com.github.mobile.android.authenticator.Constants.GITHUB_PROVIDER_AUTHORITY;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

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
        return intent.getAction().equals(ACTION_AUTHENTICATOR_INTENT) ? getAuthenticator().getIBinder() : null;
    }

    private GitHubAccountAuthenticator getAuthenticator() {
        if (sAccountAuthenticator == null)
            sAccountAuthenticator = new GitHubAccountAuthenticator(this);
        return sAccountAuthenticator;
    }

    private static void configureSyncFor(Account account) {
        Log.d(TAG, "Trying to configure account for sync...");
        setIsSyncable(account, GITHUB_PROVIDER_AUTHORITY, 1);
        setSyncAutomatically(account, GITHUB_PROVIDER_AUTHORITY, true);
        addPeriodicSync(account, GITHUB_PROVIDER_AUTHORITY, new Bundle(), (long) (15 * 60));
    }

}