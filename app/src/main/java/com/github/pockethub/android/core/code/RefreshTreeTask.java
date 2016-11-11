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
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.meisolsson.githubsdk.model.git.GitReference;
import com.meisolsson.githubsdk.model.git.GitTree;
import com.meisolsson.githubsdk.service.git.GitService;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;

/**
 * Task to load the tree for a repo's default branch
 */
public class RefreshTreeTask implements Observable.OnSubscribe<FullTree> {

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

    @Override
    public void call(Subscriber<? super FullTree> subscriber) {
        GitReference ref = reference;
        String branch = RefUtils.getPath(ref);
        if (branch == null) {
            branch = repo.defaultBranch();
            if (TextUtils.isEmpty(branch)) {
                branch = ServiceGenerator.createService(context, RepositoryService.class)
                        .getRepository(repo.owner().login(), repo.name())
                        .toBlocking()
                        .first()
                        .defaultBranch();
                if (TextUtils.isEmpty(branch))
                    subscriber.onError(new IOException(
                            "Repository does not have master branch"));
            }
        }

        GitService gitService = ServiceGenerator.createService(context, GitService.class);

        if (!isValidRef(ref)) {
            branch = branch.replace("heads/", "");
            ref = gitService.getGitReference(repo.owner().login(), repo.name(), branch)
                    .toBlocking().first();
            if (!isValidRef(ref)) {
                subscriber.onError(new IOException("Reference does not have associated commit SHA-1"));
                return;
            }
        }

        GitCommit commit = gitService.getGitCommit(repo.owner().login(), repo.name(), ref.object().sha())
                .toBlocking()
                .first();
        if (commit == null || commit.tree() == null || TextUtils.isEmpty(commit.tree().sha())) {
            subscriber.onError(new IOException("Commit does not have associated tree SHA-1"));
            return;
        }

        GitTree tree = gitService.getGitTreeRecursive(repo.owner().login(), repo.name(), commit.tree().sha())
                .toBlocking()
                .first();
        subscriber.onNext(new FullTree(tree, ref));
    }
}
