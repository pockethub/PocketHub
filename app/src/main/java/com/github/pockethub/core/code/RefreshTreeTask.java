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
package com.github.pockethub.core.code;

import android.accounts.Account;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.GitCommit;
import com.alorma.github.sdk.bean.dto.response.GitReference;
import com.alorma.github.sdk.bean.dto.response.GitTree;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.git.GetGitCommitClient;
import com.alorma.github.sdk.services.git.GetGitTreeClient;
import com.alorma.github.sdk.services.git.GetReferenceClient;
import com.alorma.github.sdk.services.repo.GetRepoClient;
import com.github.pockethub.accounts.AuthenticatedUserTask;
import com.github.pockethub.core.ref.RefUtils;
import com.github.pockethub.util.InfoUtils;

import java.io.IOException;

/**
 * Task to load the tree for a repository's default branch
 */
public class RefreshTreeTask extends AuthenticatedUserTask<FullTree> {

    private static final String TAG = "RefreshTreeTask";

    private final Repo repository;

    private final GitReference reference;

    /**
     * Create task to refresh repository's tree
     *
     * @param repository
     * @param reference
     * @param context
     */
    public RefreshTreeTask(final Repo repository,
            final GitReference reference, final Context context) {
        super(context);

        this.repository = repository;
        this.reference = reference;
    }

    private boolean isValidRef(GitReference ref) {
        return ref != null && ref.object != null
                && !TextUtils.isEmpty(ref.object.sha);
    }

    @Override
    protected FullTree run(Account account) throws Exception {
        GitReference ref = reference;
        String branch = RefUtils.getPath(ref);
        if (branch == null) {
            branch = repository.default_branch;
            if (TextUtils.isEmpty(branch)) {
                branch = new GetRepoClient(InfoUtils.createRepoInfo(repository))
                        .observable().toBlocking().first().default_branch;
                if (TextUtils.isEmpty(branch))
                    throw new IOException(
                            "Repo does not have master branch");
            }
            branch = "refs/heads/" + branch;
        }

        if (!isValidRef(ref)) {
            ref = new GetReferenceClient(InfoUtils.createRepoInfo(repository, branch)).observable().toBlocking().first();
            if (!isValidRef(ref))
                throw new IOException(
                        "Reference does not have associated commit SHA-1");
        }

        GitCommit commit = new GetGitCommitClient(InfoUtils.createRepoInfo(repository, ref.object.sha))
                .observable().toBlocking().first();
        if (commit == null || commit.tree == null
                || TextUtils.isEmpty(commit.tree.sha))
            throw new IOException("Commit does not have associated tree SHA-1");

        GitTree tree = new GetGitTreeClient(InfoUtils.createRepoInfo(repository, commit.tree.sha),true)
                .observable().toBlocking().first();
        return new FullTree(tree, ref);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception loading tree", e);
    }
}
