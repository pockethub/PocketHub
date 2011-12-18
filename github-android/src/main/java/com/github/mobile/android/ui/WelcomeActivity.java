package com.github.mobile.android.ui;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.mobile.android.R;
import com.github.mobile.android.authenticator.GitHubAuthenticatorActivity;
import com.google.inject.Inject;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContextScopedProvider;

/**
 * Welcome activity that prompts to sign in
 */
public class WelcomeActivity extends RoboFragmentActivity {

    private static final String TAG = "WA";
    private static final int CODE_AUTH = 0;

    @Inject ContextScopedProvider<Account> accountProvider;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (CODE_AUTH == requestCode && RESULT_OK == resultCode) {
            setResult(RESULT_OK);
            finish();
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Specified by android:onClick="signInClicked" in the layout xml
     *
     * @param view
     */
    public void signInClicked(View view) {
        startActivityForResult(new Intent(this, GitHubAuthenticatorActivity.class), CODE_AUTH);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Account account = accountProvider.get(this);
        if (account != null) {
            Log.d(TAG, "Closing Welcome activity, already have current account: "+account.name);
            finish();
        }
    }
}
