package com.github.mobile.util;

import static android.util.Log.DEBUG;
import android.util.Log;

import com.github.mobile.authenticator.GitHubAccount;
import com.google.inject.Provider;

import java.net.HttpURLConnection;

import org.eclipse.egit.github.core.client.GitHubClient;

/**
 * {@link GitHubClient} extensions that integrates with the Android account manager to provide request credentials
 */
public class AccountGitHubClient extends GitHubClient {

    private static final String TAG = "AccountGitHubClient";

    private final Provider<GitHubAccount> accountProvider;

    /**
     * Create account-aware client
     *
     * @param accountProvider
     */
    public AccountGitHubClient(final Provider<GitHubAccount> accountProvider) {
        this.accountProvider = accountProvider;
    }

    /**
     * Create account-aware client
     *
     * @param hostname
     * @param accountProvider
     */
    public AccountGitHubClient(final String hostname, final Provider<GitHubAccount> accountProvider) {
        super(hostname);
        this.accountProvider = accountProvider;
    }

    @Override
    protected HttpURLConnection configureRequest(final HttpURLConnection request) {
        GitHubAccount account = accountProvider.get();

        if (Log.isLoggable(TAG, DEBUG))
            Log.d(TAG, "Authenticating using " + account);

        // Credentials setting must come before super call
        setCredentials(account.username, account.password);

        return super.configureRequest(request);
    }
}
