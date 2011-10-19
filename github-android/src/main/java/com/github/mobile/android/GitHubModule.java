package com.github.mobile.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import com.github.mobile.android.authenticator.AccountClientProvider;

import com.github.mobile.android.authenticator.Constants;
import com.google.inject.Provides;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import roboguice.config.AbstractAndroidModule;

import java.io.IOException;

import static com.github.mobile.android.authenticator.Constants.GITHUB_ACCOUNT_TYPE;


public class GitHubModule extends AbstractAndroidModule {

	private static final String TAG = "GHMod";

	@Override
	protected void configure() {
		bind(IClientProvider.class).to(AccountClientProvider.class);
	}

    @Provides Account currentAccount(AccountManager accountManager) {
		Account[] accounts = accountManager.getAccountsByType(GITHUB_ACCOUNT_TYPE);
		if (accounts.length > 0) {
            return accounts[0]; // at some point, support more than one github account, ie vanilla and fi
        }
        return null;
    }

    @Provides GitHubClient gitHubClient(Account account, AccountManager accountManager) {
        GitHubClient client = new GitHubClient();
        if (account!=null) {
            client.setCredentials(account.name, accountManager.getPassword(account));
        }
        return client;
    }

    @Provides UserService userService(GitHubClient client) {
        return new UserService(client);
    }

    @Provides User currentUser(UserService userService) throws IOException {
        return userService.getUser(); // actually, probably better to cache this
    }
}
