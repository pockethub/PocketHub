package com.github.mobile.android.util;

import android.util.Log;

import com.github.mobile.android.authenticator.GitHubAccount;
import com.google.inject.Provider;

import java.net.HttpURLConnection;

import org.eclipse.egit.github.core.client.GitHubClient;


/**
 * Addresses the problem with making sure the user is authenticated: that getting them to sign in is
 * a process that will block, so you can't reasonably expect anything that occurs on the main thread
 * to have a guaranteed reference to authenticated GitHub credentials.
 * <p/>
 * Fortunately, when people are doing network access it <em>has</em> to be on a background thread. We can
 * enforce that these background threads have required the user to login, so at the point of <em>executing</em>
 * the request, we will have credentials and can configure them on the request.
 */
public class LateAuthenticatedGitHubClient extends GitHubClient {

    private final Provider<GitHubAccount> gitHubAccountProvider;
    private static final String TAG = "LateAuthenticatedGitHubClient";

    public LateAuthenticatedGitHubClient(Provider<GitHubAccount> gitHubAccountProvider) {
        this.gitHubAccountProvider = gitHubAccountProvider;
    }

    public LateAuthenticatedGitHubClient(String hostname, Provider<GitHubAccount> gitHubAccountProvider) {
        super(hostname);
        this.gitHubAccountProvider = gitHubAccountProvider;
    }

    @Override
    protected HttpURLConnection configureRequest(HttpURLConnection request) {
        GitHubAccount ghAccount = gitHubAccountProvider.get();
        Log.d(TAG, "Authenticating using " + ghAccount);
        setCredentials(ghAccount.username, ghAccount.password); // must come *before* super to set credentials
        super.configureRequest(request);
        return request;
    }
}
