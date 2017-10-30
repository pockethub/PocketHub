/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.android.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.MainActivity;
import com.github.pockethub.android.ui.roboactivities.RoboAccountAuthenticatorAppCompatActivity;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.core.TokenStore;
import com.meisolsson.githubsdk.model.GitHubToken;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.model.request.RequestToken;
import com.meisolsson.githubsdk.service.users.UserService;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;

import static com.github.pockethub.android.accounts.AccountConstants.PROVIDER_AUTHORITY;

/**
 * Activity to login
 */
public class LoginActivity extends RoboAccountAuthenticatorAppCompatActivity {

    /**
     * Auth token type parameter
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    /**
     * Initial user name
     */
    public static final String PARAM_USERNAME = "username";

    public static final String OAUTH_HOST = "www.github.com";

    public static final String INTENT_EXTRA_URL = "url";

    private static int WEBVIEW_REQUEST_CODE = 0;

    private static final String TAG = "LoginActivity";

    private static final long SYNC_PERIOD = TimeUnit.HOURS.toSeconds(8);
    private String clientId;
    private String secret;
    private String redirectUri;

    public static void configureSyncFor(Account account) {
        Log.d(TAG, "Configuring account sync");

        ContentResolver.setIsSyncable(account, PROVIDER_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, PROVIDER_AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, PROVIDER_AUTHORITY,
            new Bundle(), SYNC_PERIOD);
    }

    private AccountManager accountManager;

    private Account[] accounts;

    private String accessToken;

    private String scope;

    private MaterialDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        clientId = getString(R.string.github_client);
        secret = getString(R.string.github_secret);
        redirectUri = getString(R.string.github_oauth);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        accountManager = AccountManager.get(this);

        accounts = accountManager.getAccountsByType(getString(R.string.account_type));

        if (accounts != null && accounts.length > 0) {
            openMain();
        }
        checkOauthConfig();
    }

    private void checkOauthConfig() {
        if (clientId.equals("dummy_client") || secret.equals("dummy_secret")) {
            Toast.makeText(this, R.string.error_oauth_not_configured, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        onUserLoggedIn(uri);
    }

    private void onUserLoggedIn(Uri uri) {
        if (uri != null && uri.getScheme().equals(getString(R.string.github_oauth_scheme))) {
            openLoadingDialog();
            String code = uri.getQueryParameter("code");
            RequestToken request = RequestToken.builder()
                    .clientId(clientId)
                    .clientSecret(secret)
                    .redirectUri(redirectUri)
                    .code(code)
                    .build();

            ServiceGenerator.createAuthService()
                    .getToken(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(this.bindToLifecycle())
                    .subscribe(response -> {
                        GitHubToken token = response.body();
                        if (token.accessToken() != null) {
                            endAuth(token.accessToken(), token.scope());
                        } else if (token.error() != null) {
                            Toast.makeText(this, token.error(), Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }, Throwable::printStackTrace);
        }
    }

    private void openMain() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void openLoadingDialog() {
        progressDialog = new MaterialDialog.Builder(this)
                .progress(true, 0)
                .content(R.string.login_activity_authenticating)
                .show();
    }

    public void handleLogin() {
        openLoginInBrowser();
    }

    private void openLoginInBrowser() {
        String initialScope = "user,public_repo,repo,delete_repo,notifications,gist";
        HttpUrl.Builder url = new HttpUrl.Builder()
                .scheme("https")
                .host(OAUTH_HOST)
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", getString(R.string.github_client))
                .addQueryParameter("scope", initialScope);

        Intent intent = new Intent(this, LoginWebViewActivity.class);
        intent.putExtra(INTENT_EXTRA_URL, url.toString());
        startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == WEBVIEW_REQUEST_CODE && resultCode == RESULT_OK) {
            onUserLoggedIn(data.getData());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_login:
                handleLogin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void endAuth(final String accessToken, final String scope) {
        this.accessToken = accessToken;
        this.scope = scope;

        progressDialog.setContent(getString(R.string.loading_user));

        TokenStore.getInstance(this).saveToken(accessToken);
        ServiceGenerator.createService(this, UserService.class)
                .getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(response -> {
                    User user = response.body();
                    Account account = new Account(user.login(), getString(R.string.account_type));
                    Bundle userData = AccountsHelper.buildBundle(user.name(),
                            user.email(), user.avatarUrl(), scope);
                    userData.putString(AccountManager.KEY_AUTHTOKEN, accessToken);

                    accountManager.addAccountExplicitly(account, null, userData);
                    accountManager.setAuthToken(account, getString(R.string.account_type), accessToken);

                    Bundle result = new Bundle();
                    result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                    result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                    result.putString(AccountManager.KEY_AUTHTOKEN, accessToken);

                    setAccountAuthenticatorResult(result);

                    openMain();
                }, Throwable::printStackTrace);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getMenuInflater().inflate(R.menu.activity_login, optionMenu);
        return true;
    }
}
