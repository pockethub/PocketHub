package com.github.mobile.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.github.mobile.android.R;
import com.github.mobile.android.authenticator.GitHubAuthenticatorActivity;

public class WelcomeActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
    }

    /**
     * Specified by android:onClick="signInClicked" in the layout xml
     */
    public void signInClicked(View view) {
        startActivityForResult(new Intent(this, GitHubAuthenticatorActivity.class), 0);
        finish();
    }
}
