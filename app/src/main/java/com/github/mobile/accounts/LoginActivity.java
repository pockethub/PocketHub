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
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static com.github.mobile.accounts.Constants.AUTH_TOKEN_TYPE;
import static com.github.mobile.accounts.Constants.GITHUB_ACCOUNT_TYPE;
import static com.github.mobile.accounts.Constants.GITHUB_PROVIDER_AUTHORITY;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.DefaultClient;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.ui.LightProgressDialog;
import com.github.mobile.ui.TextWatcherAdapter;
import com.github.mobile.util.ToastUtils;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockAccountAuthenticatorActivity;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.UserService;

import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to login
 */
public class LoginActivity extends RoboSherlockAccountAuthenticatorActivity {

    /**
     * Auth token type parameter
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";

    private static final String PARAM_USERNAME = "username";

    private static final String TAG = "LoginActivity";

    private static void configureSyncFor(Account account) {
        Log.d(TAG, "Configuring account sync");

        ContentResolver.setIsSyncable(account, GITHUB_PROVIDER_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account,
                GITHUB_PROVIDER_AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, GITHUB_PROVIDER_AUTHORITY,
                new Bundle(), 15L * 60L);
    }

    private static class AccountLoader extends
            AuthenticatedUserTask<List<User>> {

        @Inject
        private AccountDataManager cache;

        protected AccountLoader(Context context) {
            super(context);
        }

        @Override
        protected List<User> run() throws Exception {
            return cache.getOrgs();
        }
    }

    private AccountManager accountManager;

    @InjectView(id.et_login)
    private AutoCompleteTextView loginText;

    @InjectView(id.et_password)
    private EditText passwordText;

    private RoboAsyncTask<User> authenticationTask;

    private String authToken;

    private String authTokenType;

    private MenuItem loginItem;

    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password to be changed on the device.
     */
    private Boolean confirmCredentials = false;

    private String password;

    /**
     * Was the original caller asking for an entirely new account?
     */
    protected boolean requestNewAccount = false;

    private String username;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.login);

        accountManager = AccountManager.get(this);

        final Intent intent = getIntent();
        username = intent.getStringExtra(PARAM_USERNAME);
        authTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        requestNewAccount = username == null;
        confirmCredentials = intent.getBooleanExtra(PARAM_CONFIRMCREDENTIALS,
                false);

        TextView signupText = (TextView) findViewById(id.tv_signup);
        signupText.setMovementMethod(LinkMovementMethod.getInstance());
        signupText.setText(Html.fromHtml(getString(string.signup_link)));

        TextWatcher watcher = new TextWatcherAdapter() {

            @Override
            public void afterTextChanged(Editable gitDirEditText) {
                updateEnablement();
            }
        };
        loginText.addTextChangedListener(watcher);
        passwordText.addTextChangedListener(watcher);

        passwordText.setOnKeyListener(new OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event != null && ACTION_DOWN == event.getAction()
                        && keyCode == KEYCODE_ENTER && loginEnabled()) {
                    handleLogin();
                    return true;
                } else
                    return false;
            }
        });

        passwordText.setOnEditorActionListener(new OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == IME_ACTION_DONE && loginEnabled()) {
                    handleLogin();
                    return true;
                }
                return false;
            }
        });

        loginText.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                getEmailAddresses()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateEnablement();
    }

    private boolean loginEnabled() {
        return !TextUtils.isEmpty(loginText.getText())
                && !TextUtils.isDigitsOnly(passwordText.getText());
    }

    private void updateEnablement() {
        if (loginItem != null)
            loginItem.setEnabled(loginEnabled());
    }

    /**
     * Authenticate login & password
     */
    public void handleLogin() {
        if (requestNewAccount)
            username = loginText.getText().toString();
        password = passwordText.getText().toString();

        final ProgressDialog dialog = LightProgressDialog.create(this,
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
                GitHubClient client = new DefaultClient();
                client.setCredentials(username, password);
                User user = new UserService(client).getUser();

                Account account = new Account(user.getLogin(),
                        GITHUB_ACCOUNT_TYPE);
                if (requestNewAccount) {
                    accountManager
                            .addAccountExplicitly(account, password, null);
                    configureSyncFor(account);
                    try {
                        new AccountLoader(LoginActivity.this).call();
                    } catch (IOException e) {
                        Log.d(TAG, "Exception loading organizations", e);
                    }
                } else
                    accountManager.setPassword(account, password);

                return user;
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                dialog.dismiss();

                Log.d(TAG, "Exception requesting authenticated user", e);

                Throwable cause = e.getCause() != null ? e.getCause() : e;

                boolean badCredentials = false;
                if (e instanceof RequestException
                        && ((RequestException) e).getStatus() == 401)
                    badCredentials = true;
                // A 401 can be returned as an IOException with this message
                else if ("Received authentication challenge is null"
                        .equals(cause.getMessage()))
                    badCredentials = true;

                if (badCredentials)
                    onAuthenticationResult(false);
                else
                    ToastUtils.show(LoginActivity.this, e,
                            string.connection_failed);
            }

            @Override
            public void onSuccess(User user) {
                dialog.dismiss();

                onAuthenticationResult(true);
            }
        };
        authenticationTask.execute();
    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
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
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. Also sets
     * the authToken in AccountManager for this account.
     */

    protected void finishLogin() {
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
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getSupportMenuInflater().inflate(menu.login, optionMenu);
        loginItem = optionMenu.findItem(id.m_login);
        return true;
    }

    private List<String> getEmailAddresses() {
        final Account[] accounts = accountManager
                .getAccountsByType("com.google");
        final List<String> addresses = new ArrayList<String>(accounts.length);
        for (Account account : accounts)
            addresses.add(account.name);
        return addresses;
    }
}
