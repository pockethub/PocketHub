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

package com.github.pockethub.core.repo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.R;
import com.github.pockethub.accounts.AccountAuthenticator;
import com.github.pockethub.api.GitHubClientV2;
import com.github.pockethub.model.Authorization;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.InfoUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.util.EncodingUtils;

import java.util.Collections;
import java.util.List;

/**
 * Task to fork a repository
 */
public class DeleteRepositoryTask extends ProgressDialogTask<Void> {
    private static final String TAG = "DeleteRepositoryTask";

    @Inject
    private RepositoryService repositoryService;

    @Inject
    private OAuthService oAuthService;

    private final Repo repo;

    /**
     * Create task for context and id provider
     *
     * @param context
     * @param repo
     */
    public DeleteRepositoryTask(Context context, Repo repo) {
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
        final String id = InfoUtils.createRepoId(repo);
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
