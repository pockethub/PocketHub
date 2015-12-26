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
package com.github.pockethub.accounts;

import android.text.TextUtils;
import android.util.Log;

import com.github.pockethub.DefaultClient;
import com.google.inject.Provider;

import org.eclipse.egit.github.core.client.GitHubClient;

import java.net.HttpURLConnection;

import static android.util.Log.DEBUG;

/**
 * {@link GitHubClient} extensions that integrates with the Android account
 * manager to provide request credentials
 */
public class AccountClient extends DefaultClient {

    private static final String TAG = "AccountGitHubClient";

    private final Provider<GitHubAccount> accountProvider;

    /**
     * Create account-aware client
     *
     * @param accountProvider
     */
    public AccountClient(final Provider<GitHubAccount> accountProvider) {
        super();

        this.accountProvider = accountProvider;
    }

    @Override
    protected HttpURLConnection configureRequest(final HttpURLConnection request) {
        GitHubAccount account = accountProvider.get();

        if (Log.isLoggable(TAG, DEBUG))
            Log.d(TAG, "Authenticating using " + account);

        // Credentials setting must come before super call
        String token = account.getAuthToken();
        if (!TextUtils.isEmpty(token))
            setOAuth2Token(token);
        else
            setCredentials(account.getUsername(), account.getPassword());

        return super.configureRequest(request);
    }
}
