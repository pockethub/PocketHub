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

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static com.github.mobile.accounts.Constants.AUTH_TOKEN_TYPE;
import static com.github.mobile.accounts.Constants.GITHUB_ACCOUNT_TYPE;
import static com.github.mobile.accounts.Constants.GITHUB_PROVIDER_AUTHORITY;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mobile.DefaultClient;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.ui.BlankTextFieldWarner;
import com.github.mobile.ui.LightProgressDialog;
import com.github.mobile.ui.TextWatcherAdapter;
import com.github.mobile.util.ToastUtils;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockAccountAuthenticatorActivity;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.UserService;

import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to login
 */
public class AuthenticatorActivity extends RoboSherlockAccountAuthenticatorActivity {

    /**
     * Auth token type parameter
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";

    private static final String PARAM_USERNAME = "username";

    private static final String TAG = "GHAuthenticatorActivity";

    private static void configureSyncFor(Account account) {
        Log.d(TAG, "Trying to configure account for sync...");
        ContentResolver.setIsSyncable(account, GITHUB_PROVIDER_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, GITHUB_PROVIDER_AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, GITHUB_PROVIDER_AUTHORITY, new Bundle(), 15L * 60L);
    }

    private AccountManager accountManager;

    @InjectView(id.et_login)
    private EditText usernameEdit;

    @InjectView(id.et_password)
    private EditText passwordEdit;

    @InjectView(id.b_login)
    private Button okButton;

    @Inject
    private BlankTextFieldWarner leavingBlankTextFieldWarner;

    private TextWatcher watcher = validationTextWatcher();

    private RoboAsyncTask<User> authenticationTask;

    private String authToken;

    private String authTokenType;

    /**
     * If set we are just checking that the user knows their credentials; this doesn't cause the user's password to be
     * changed on the device.
     */
    private Boolean confirmCredentials = false;

    private String password;

    /**
     * Was the original caller asking for an entirely new account?
     */
    protected boolean requestNewAccount = false;

    private String username;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        accountManager = AccountManager.get(this);

        final Intent intent = getIntent();
        username = intent.getStringExtra(PARAM_USERNAME);
        authTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        requestNewAccount = username == null;
        confirmCredentials = intent.getBooleanExtra(PARAM_CONFIRMCREDENTIALS, false);

        setContentView(layout.login);

        TextView signupText = (TextView) findViewById(id.tv_signup);
        signupText.setMovementMethod(LinkMovementMethod.getInstance());
        signupText.setText(Html.fromHtml(getString(string.signup_link)));

        setNonBlankValidationFor(usernameEdit);
        setNonBlankValidationFor(passwordEdit);
    }

    private void setNonBlankValidationFor(EditText editText) {
        editText.addTextChangedListener(watcher);
        editText.setOnFocusChangeListener(leavingBlankTextFieldWarner);
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {

            public void afterTextChanged(Editable gitDirEditText) {
                updateUIWithValidation();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateUIWithValidation();
    }

    private void updateUIWithValidation() {
        boolean populated = populated(usernameEdit) && populated(passwordEdit);
        okButton.setEnabled(populated);
    }

    private boolean populated(EditText editText) {
        return editText.length() > 0;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new LightProgressDialog(this, getText(string.login_activity_authenticating));
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (authenticationTask != null)
                    authenticationTask.cancel(true);
            }
        });
        return dialog;
    }

    /**
     * Handles onClick event on the Submit button. Sends username/password to the server for authentication.
     * <p/>
     * Specified by android:onClick="handleLogin" in the layout xml
     *
     * @param view
     */
    public void handleLogin(View view) {
        if (requestNewAccount)
            username = usernameEdit.getText().toString();
        password = passwordEdit.getText().toString();

        showProgress();

        authenticationTask = new RoboAsyncTask<User>(this) {
            public User call() throws Exception {
                GitHubClient client = new DefaultClient();
                client.setCredentials(username, password);

                return new UserService(client).getUser();
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Exception requesting authenticated user", e);

                if (e instanceof RequestException && ((RequestException) e).getStatus() == 401)
                    onAuthenticationResult(false);
                else
                    ToastUtils.show(AuthenticatorActivity.this, e, string.connection_failed);
            }

            public void onSuccess(User user) {
                onAuthenticationResult(true);
            }

            @Override
            protected void onFinally() throws RuntimeException {
                hideProgress();
            }
        };
        authenticationTask.execute();
    }

    /**
     * Called when response is received from the server for confirm credentials request. See onAuthenticationResult().
     * Sets the AccountAuthenticatorResult which is sent back to the caller.
     *
     * @param result
     */
    protected void finishConfirmCredentials(boolean result) {
        final Account account = new Account(username, GITHUB_ACCOUNT_TYPE);
        accountManager.setPassword(account, password);

        final Intent intent = new Intent();
        intent.putExtra(KEY_BOOLEAN_RESULT, result);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when response is received from the server for authentication request. See onAuthenticationResult(). Sets
     * the AccountAuthenticatorResult which is sent back to the caller. Also sets the authToken in AccountManager for
     * this account.
     */

    protected void finishLogin() {
        final Account account = new Account(username, GITHUB_ACCOUNT_TYPE);

        if (requestNewAccount) {
            accountManager.addAccountExplicitly(account, password, null);
            configureSyncFor(account);
        } else
            accountManager.setPassword(account, password);

        final Intent intent = new Intent();
        authToken = password;
        intent.putExtra(KEY_ACCOUNT_NAME, username);
        intent.putExtra(KEY_ACCOUNT_TYPE, GITHUB_ACCOUNT_TYPE);
        if (authTokenType != null && authTokenType.equals(AUTH_TOKEN_TYPE))
            intent.putExtra(KEY_AUTHTOKEN, authToken);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    @SuppressWarnings("deprecation")
    private void hideProgress() {
        dismissDialog(0);
    }

    @SuppressWarnings("deprecation")
    private void showProgress() {
        showDialog(0);
    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     *
     * @param result
     */
    public void onAuthenticationResult(boolean result) {
        if (result) {
            if (!confirmCredentials)
                finishLogin();
            else
                finishConfirmCredentials(true);
        } else {
            if (requestNewAccount)
                ToastUtils.show(this, string.invalid_login_or_password);
            else
                ToastUtils.show(this, string.invalid_password);
        }
    }
}
