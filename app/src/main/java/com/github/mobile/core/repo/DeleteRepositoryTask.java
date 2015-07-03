package com.github.mobile.core.repo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import com.github.mobile.R;
import com.github.mobile.accounts.AccountAuthenticator;
import com.github.mobile.api.GitHubClientV2;
import com.github.mobile.model.Authorization;
import com.github.mobile.ui.ProgressDialogTask;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.util.EncodingUtils;

/**
 * Task to fork a repository
 */
public class DeleteRepositoryTask extends ProgressDialogTask<Void> {
    private static final String TAG = "DeleteRepositoryTask";

    @Inject
    private RepositoryService repositoryService;

    @Inject
    private OAuthService oAuthService;

    private final IRepositoryIdProvider repo;

    /**
     * Create task for context and id provider
     *
     * @param context
     * @param repo
     */
    public DeleteRepositoryTask(Context context, IRepositoryIdProvider repo) {
        super(context);
        this.repo = repo;
    }

    /**
     * Execute the task with a progress dialog displaying.
     * <p>
     * This method must be called from the main thread.
     */
    public void start() {
        showIndeterminate(R.string.deleting_repository);

        execute();
    }

    @Override
    protected Void run(Account account) throws Exception {
        final String id = repo.generateId();
        String[] paths = id.split("/");
        final String owner = paths[0];
        final String repository = paths[1];
        String credentials = null;
        String authToken = null;
        String deleteToken = null;

        AccountManager am = AccountManager.get(context);
        String password = am.getPassword(account);

        System.out.println("password: " + password);
        System.out.println("owner: " + account.name);

        if (password == null) {
            AccountAuthenticator.getAuthorization(oAuthService);
        } else {
            credentials = "Basic " + EncodingUtils.toBase64(account.name + ':' + password);
        }

        List<Authorization> authorizations = GitHubClientV2.getServiceClient().
                getAuthorizations(credentials);

        for (Authorization auth : authorizations) {
            List<String> scopes = auth.getScopes();

            if (scopes.size() == 1 && scopes.get(0).equalsIgnoreCase("delete_repo")) {
                authToken = auth.getToken();
            }
        }

        if (authToken != null) {
            deleteToken = "token " + authToken;
        } else {
            Authorization authorization = new Authorization();
            authorization.setNote("Token for deleting repositories");
            authorization.setScopes(Collections.singletonList("delete_repo"));

            Authorization authorizationResponse = GitHubClientV2.getServiceClient().
                    createDeleteAuthorization(credentials, authorization);

            if (authorizationResponse != null) {
                deleteToken = "token " + authorizationResponse.getToken();
            }
        }

        GitHubClientV2.getServiceClient().deleteRepository(deleteToken, owner, repository);

        return null;
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception deleting repository", e);
    }
}
