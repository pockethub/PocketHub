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

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.CATEGORY_BROWSABLE;
import static com.github.mobile.accounts.AccountConstants.PROVIDER_AUTHORITY;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alorma.github.basesdk.ApiClient;
import com.alorma.github.basesdk.client.BaseClient;
import com.alorma.github.sdk.bean.dto.response.Token;
import com.alorma.github.sdk.login.AccountsHelper;
import com.alorma.github.sdk.security.GitHub;
import com.alorma.github.sdk.services.login.RequestTokenClient;
import com.alorma.github.sdk.services.user.GetAuthUserClient;
import com.github.mobile.R;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.ui.LightProgressDialog;
import com.github.mobile.ui.MainActivity;
import com.github.mobile.ui.roboactivities.RoboActionBarAccountAuthenticatorActivity;
import com.google.inject.Inject;
import com.squareup.okhttp.HttpUrl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.User;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Activity to login
 */
public class LoginActivity extends RoboActionBarAccountAuthenticatorActivity implements BaseClient.OnResultCallback<com.alorma.github.sdk.bean.dto.response.User> {

    /**
     * Auth token type parameter
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    /**
     * Initial user name
     */
    public static final String PARAM_USERNAME = "username";

    public static final String OAUTH_HOST = "www.github.com";

    private static final String TAG = "LoginActivity";

    /**
     * Sync period in seconds, currently every 8 hours
     */
    private static final long SYNC_PERIOD = 8L * 60L * 60L;

    public static void configureSyncFor(Account account) {
        Log.d(TAG, "Configuring account sync");

        ContentResolver.setIsSyncable(account, PROVIDER_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, PROVIDER_AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, PROVIDER_AUTHORITY,
            new Bundle(), SYNC_PERIOD);
    }

    public static class AccountLoader extends
        AuthenticatedUserTask<List<User>> {

        @Inject
        private AccountDataManager cache;

        protected AccountLoader(Context context) {
            super(context);
        }

        @Override
        protected List<User> run(Account account) throws Exception {
            return cache.getOrgs(true);
        }
    }

    private AccountManager accountManager;

    private Account[] accounts;

    private String accessToken;

    private String scope;

    private RequestTokenClient requestTokenClient;

    private AlertDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        accountManager = AccountManager.get(this);

        accounts = accountManager.getAccountsByType(getString(R.string.account_type));

        if (accounts != null && accounts.length > 0)
            openMain();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if(uri != null && uri.getScheme().equals(getString(R.string.github_oauth_scheme))){
            openLoadingDialog();
            String code = uri.getQueryParameter("code");

            if (requestTokenClient == null) {
                requestTokenClient = new RequestTokenClient(LoginActivity.this, code);
                requestTokenClient.setOnResultCallback(new BaseClient.OnResultCallback<Token>() {
                    @Override
                    public void onResponseOk(Token token, Response r) {
                        if (token.access_token != null) {
                            endAccess(token.access_token, token.scope);
                        } else if (token.error != null) {
                            Toast.makeText(LoginActivity.this, token.error, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFail(RetrofitError error) {
                        error.printStackTrace();
                    }
                });
                requestTokenClient.execute();
            }
        }
    }

    private void openMain() {
        progressDialog.dismiss();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void openLoadingDialog() {
        progressDialog = LightProgressDialog.create(this,
                R.string.login_activity_authenticating);
        progressDialog.show();
    }

    @Override
    public void startActivity(Intent intent) {
        if (intent != null && ACTION_VIEW.equals(intent.getAction()))
            intent.addCategory(CATEGORY_BROWSABLE);

        super.startActivity(intent);
    }

    public void handleLogin() {
        openLoginInBrowser(new GitHub(this));
    }

    private void openLoginInBrowser(ApiClient client) {
        String initialScope = "user,public_repo,repo,delete_repo,notifications,gist";
        HttpUrl.Builder url = new HttpUrl.Builder()
                .scheme("https")
                .host(OAUTH_HOST)
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", client.getApiClient())
                .addQueryParameter("scope", initialScope);

        final List<ResolveInfo> browserList = getBrowserList();

        final List<LabeledIntent> intentList = new ArrayList<>();

        for (final ResolveInfo resolveInfo : browserList) {
            final Intent newIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.build().toString()));
            newIntent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name));

            intentList.add(new LabeledIntent(newIntent,
                    resolveInfo.resolvePackageName,
                    resolveInfo.labelRes,
                    resolveInfo.icon));
        }

        final Intent chooser = Intent.createChooser(intentList.remove(0), "Choose your favorite browser");
        LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

        startActivity(chooser);
    }

    private List<ResolveInfo> getBrowserList() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://sometesturl.com"));

        return getPackageManager().queryIntentActivities(intent, 0);
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

    @Override
    public void onResponseOk(com.alorma.github.sdk.bean.dto.response.User user, Response r) {
        Account account = new Account(user.login, getString(R.string.account_type));
        Bundle userData = AccountsHelper.buildBundle(user.name, user.email, user.avatar_url, scope);
        userData.putString(AccountManager.KEY_AUTHTOKEN, accessToken);

        accountManager.addAccountExplicitly(account, null, userData);
        accountManager.setAuthToken(account, getString(R.string.account_type), accessToken);

        Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        result.putString(AccountManager.KEY_AUTHTOKEN, accessToken);

        setAccountAuthenticatorResult(result);

        openMain();
    }

    @Override
    public void onFail(RetrofitError error) {
        error.printStackTrace();
    }

    private void endAccess(String accessToken, String scope) {
        this.accessToken = accessToken;
        this.scope = scope;

        progressDialog.setMessage(getString(R.string.loading_user));

        GetAuthUserClient userClient = new GetAuthUserClient(this, accessToken);
        userClient.setOnResultCallback(this);
        userClient.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getMenuInflater().inflate(R.menu.login, optionMenu);
        return true;
    }
}