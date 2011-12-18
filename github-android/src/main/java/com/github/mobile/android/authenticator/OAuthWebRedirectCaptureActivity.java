package com.github.mobile.android.authenticator;


import static com.github.mobile.android.authenticator.Constants.AUTHTOKEN_TYPE;
import static com.github.mobile.android.authenticator.Constants.GITHUB_ACCOUNT_TYPE;
import static com.github.mobile.android.authenticator.OAuth.accessTokenFor;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.github.mobile.android.HomeActivity;
import com.google.inject.Inject;

import java.io.IOException;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;

import roboguice.activity.RoboFragmentActivity;
import roboguice.util.RoboAsyncTask;

public class OAuthWebRedirectCaptureActivity extends RoboFragmentActivity {

    private static final String TAG="OAWRCA";

    @Inject AccountManager am;

    @Override
    protected void onStart() {
    	super.onStart();
        final Uri uri = getIntent().getData();
        Log.d(TAG, "uri = " + uri);
        final String tempCode = uri.getQueryParameter("code");
        
        new RoboAsyncTask<Void>(this) {
            public Void call() throws Exception {
                createOrUpdateAccountFrom(accessTokenFor(tempCode));
                return null;
            }

            protected void onSuccess(Void v) throws Exception {
                // unfortunately, the browser will still be left open, but there's no way to close it
                startActivity(new Intent(OAuthWebRedirectCaptureActivity.this, HomeActivity.class));
                finish();
            };
        }.execute();
    }

    private void createOrUpdateAccountFrom(String accessToken) {
        User user = userFor(accessToken);
        Account account = getOrCreateAccountFor(user);
        am.setAuthToken(account, AUTHTOKEN_TYPE, accessToken);
        Log.d(TAG, "Set authtoken for "+account.name);
    }

    private User userFor(String accessToken) {
        GitHubClient ghc= new GitHubClient();
        ghc.setOAuth2Token(accessToken);
        UserService userService = new UserService(ghc);
        try {
            User user = userService.getUser();
            Log.d(TAG, "user " + user.getName());
            return user;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Account getOrCreateAccountFor(User user) {
        String userLogin = user.getLogin();
        for (Account account : am.getAccountsByType(GITHUB_ACCOUNT_TYPE)) {
            if (userLogin.equals(account.name))
                return account;
        }
        Account newAccount = new Account(userLogin, GITHUB_ACCOUNT_TYPE);
        am.addAccountExplicitly(newAccount, null, null);
        return newAccount;
    }
}