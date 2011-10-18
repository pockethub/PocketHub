package com.github.mobile.android;

import com.github.mobile.android.authenticator.AccountClientProvider;

import roboguice.config.AbstractAndroidModule;

public class GitHubModule extends AbstractAndroidModule {

	private static final String TAG = "GHMod";

	@Override
	protected void configure() {
		bind(IClientProvider.class).to(AccountClientProvider.class);
	}
}
