package com.github.mobile.authenticator;

import static android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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

}