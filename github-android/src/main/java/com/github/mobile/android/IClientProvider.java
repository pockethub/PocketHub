package com.github.mobile.android;

import android.content.Context;

import org.eclipse.egit.github.core.client.GitHubClient;

/**
 * Interface that provides a client for a context
 */
public interface IClientProvider {

	/**
	 * Create client for context
	 * 
	 * @param context
	 * @return non-null client
	 */
	GitHubClient createClient(Context context);
}
