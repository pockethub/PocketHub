/*
 * Copyright 2013 GitHub Inc.
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

import static android.content.DialogInterface.OnCancelListener;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static com.github.mobile.accounts.AccountConstants.*;
import static com.github.mobile.accounts.LoginActivity.configureSyncFor;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.kevinsawicki.wishlist.ViewFinder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.ui.LightProgressDialog;
import com.github.mobile.ui.TextWatcherAdapter;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;

import java.io.IOException;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.UserService;

import roboguice.util.RoboAsyncTask;

/**
 * Activity to enter two-factor authentication OTP code
 */
public class TwoFactorAuthActivity extends RoboSherlockActivity {

    /**
     * Create intent to enter two-factor authentication code
     *
     * @param username
     * @param password
     * @return
     */
    public static Intent createIntent(Context context, String username, String password) {
        Intent intent = new Intent(context, TwoFactorAuthActivity.class);
        intent.putExtra(PARAM_USERNAME, username);
        intent.putExtra(PARAM_PASSWORD, password);
        return intent;
    }

    /**
     * Exception sent back to calling Activity
     */
    public static final String PARAM_EXCEPTION = "exception";

    /**
     * User name entered in login screen
     */
    public static final String PARAM_USERNAME = "username";

    /**
     * Password entered in login screen
     */
    public static final String PARAM_PASSWORD = "password";

    private static final String TAG = "TwoFactorAuthActivity";

    private AccountManager accountManager;

    private EditText otpCodeText;

    private RoboAsyncTask<User> authenticationTask;

    private MenuItem loginItem;

    private String username;

    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.login_two_factor_auth);

        accountManager = AccountManager.get(this);

        ViewFinder finder = new ViewFinder(this);
        otpCodeText = finder.find(id.et_otp_code);

        final Intent intent = getIntent();
        username = intent.getStringExtra(PARAM_USERNAME);
        password = intent.getStringExtra(PARAM_PASSWORD);

        TextView signupText = finder.find(id.tv_signup);
        signupText.setMovementMethod(LinkMovementMethod.getInstance());
        signupText.setText(Html.fromHtml(getString(string.signup_link_two_factor_auth)));

        TextWatcher watcher = new TextWatcherAdapter() {

            @Override
            public void afterTextChanged(Editable gitDirEditText) {
                updateEnablement();
            }
        };
        otpCodeText.addTextChangedListener(watcher);

        otpCodeText.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event != null && ACTION_DOWN == event.getAction()
                        && keyCode == KEYCODE_ENTER && loginEnabled()) {
                    handleLogin();
                    return true;
                } else
                    return false;
            }
        });

        otpCodeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == IME_ACTION_DONE && loginEnabled()) {
                    handleLogin();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateEnablement();
    }

    private boolean loginEnabled() {
        Editable otpCode = otpCodeText.getText();
        return !TextUtils.isEmpty(otpCode) && otpCode.length() == 6;
    }

    private void updateEnablement() {
        if (loginItem != null)
            loginItem.setEnabled(loginEnabled());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case id.m_login:
                handleLogin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionsMenu) {
        getSupportMenuInflater().inflate(menu.login, optionsMenu);
        loginItem = optionsMenu.findItem(id.m_login);
        return true;
    }

    private void handleLogin() {
        final String otpCode = otpCodeText.getText().toString();

        final AlertDialog dialog = LightProgressDialog.create(this,
                string.login_activity_authenticating);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (authenticationTask != null)
                    authenticationTask.cancel(true);
            }
        });
        dialog.show();

        authenticationTask = new RoboAsyncTask<User>(this) {

            @Override
            public User call() throws Exception {
                TwoFactorAuthClient client = new TwoFactorAuthClient();
                client.setCredentials(username, password);
                client.setOtpCode(otpCode);

                OAuthService service = new OAuthService(client);
                String authToken = AccountAuthenticator.getAuthorization(service);
                if (authToken == null)
                  authToken = AccountAuthenticator.createAuthorization(service);
                client.setOAuth2Token(authToken);

                User user = new UserService(client).getUser();
                Account account = new Account(user.getLogin(), ACCOUNT_TYPE);
                accountManager.addAccountExplicitly(account, password, null);
                accountManager.setAuthToken(account, ACCOUNT_TYPE, authToken);

                configureSyncFor(account);
                try {
                    new LoginActivity.AccountLoader(TwoFactorAuthActivity.this).call();
                } catch (IOException e) {
                    Log.d(TAG, "Exception loading organizations", e);
                }

                return user;
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                dialog.dismiss();

                Log.d(TAG, "Exception requesting handling two-factor authentication", e);
                setResult(RESULT_CANCELED, new Intent().putExtra(PARAM_EXCEPTION, e));
                finish();
            }

            @Override
            public void onSuccess(User user) {
                dialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }
        };
        authenticationTask.execute();
    }
}
