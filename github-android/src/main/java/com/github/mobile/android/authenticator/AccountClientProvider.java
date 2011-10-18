package com.github.mobile.android.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.github.mobile.android.IClientProvider;

import org.eclipse.egit.github.core.client.GitHubClient;

/**
 * Provide authenticated client using first account found in the
 * {@link AccountManager}
 */
public class AccountClientProvider implements IClientProvider {

	public GitHubClient createClient(Context context) {
		AccountManager manager = AccountManager.get(context);
		GitHubClient client = new GitHubClient();
		Account[] accounts = manager
				.getAccountsByType(Constants.GITHUB_ACCOUNT_TYPE);
		if (accounts.length > 0)
			client.setCredentials(accounts[0].name,
					manager.getPassword(accounts[0]));
		return client;
	}
}
