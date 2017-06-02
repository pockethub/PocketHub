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
package com.github.pockethub.android.core.code;

import android.content.Context;
import android.text.TextUtils;

import com.github.pockethub.android.core.ref.RefUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.meisolsson.githubsdk.model.git.GitReference;
import com.meisolsson.githubsdk.model.git.GitTree;
import com.meisolsson.githubsdk.service.git.GitService;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;

import java.io.IOException;
import java.sql.Ref;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import retrofit2.Response;

/**
 * Task to load the tree for a repo's default branch
 */
public class RefreshTreeTask {

    private static final String TAG = "RefreshTreeTask";

    private final Context context;

    private final Repository repo;

    private final GitReference reference;

    /**
     * Create task to refresh repo's tree
     *
     * @param repository
     * @param reference
     */
    public RefreshTreeTask(final Context context, final Repository repository,
                           final GitReference reference) {
        this.context = context;
        this.repo = repository;
        this.reference = reference;
    }

    private boolean isValidRef(GitReference ref) {
        return ref != null && ref.object() != null
                && !TextUtils.isEmpty(ref.object().sha());
    }

    private Single<GitReference> getValidRef(GitService service, GitReference ref, String branch) {
        if (!isValidRef(ref)) {
            return service.getGitReference(repo.owner().login(), repo.name(), branch)
                    .map(response -> {
                        if (response.isSuccessful()) {
                            GitReference fetchedRef = response.body();
                            if (isValidRef(fetchedRef)) {
                                return fetchedRef;
                            } else {
                                throw new IOException("Reference does not have associated commit SHA-1");
                            }
                        } else {
                            throw new IOException("Request for Git Reference was unsuccessful");
                        }
                    });
        }

        return Single.just(ref);
    }

    private Single<String> getBranch(GitReference ref) {
        String branch = RefUtils.getPath(ref);
        if (branch == null) {
            branch = repo.defaultBranch();
            if (TextUtils.isEmpty(branch)) {
                return ServiceGenerator
                        .createService(context, RepositoryService.class)
                        .getRepository(repo.owner().login(), repo.name())
                        .map(response -> response.body().defaultBranch());
            }
        }

        return Single.just(branch);
    }

    public Single<FullTree> refresh() {
        GitService gitService = ServiceGenerator.createService(context, GitService.class);

        return getBranch(reference)
                .map(branch -> branch.replace("heads/", ""))
                .flatMap(branch -> getValidRef(gitService, reference, branch))
                .flatMap(reference ->
                        gitService.getGitCommit(repo.owner().login(), repo.name(),
                                reference.object().sha())
                                .map(Response::body)
                                .zipWith(Single.just(reference), RefreshTreeModel::new))
                .flatMap(model ->
                        gitService.getGitTreeRecursive(repo.owner().login(),
                                repo.name(), model.getCommit().tree().sha())
                                .map(Response::body)
                                .zipWith(Single.just(model.ref), FullTree::new));
    }

    private class RefreshTreeModel {
        private GitReference ref;
        private GitCommit commit;

        public RefreshTreeModel(GitCommit commit, GitReference ref) {
            this.commit = commit;
            this.ref = ref;
        }

        public GitReference getRef() {
            return ref;
        }

        public void setRef(GitReference ref) {
            this.ref = ref;
        }

        public GitCommit getCommit() {
            return commit;
        }

        public void setCommit(GitCommit commit) {
            this.commit = commit;
        }

    }
}
