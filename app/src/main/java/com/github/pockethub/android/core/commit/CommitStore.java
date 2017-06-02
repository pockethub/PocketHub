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
package com.github.pockethub.android.core.commit;

import android.content.Context;

import com.github.pockethub.android.core.ItemStore;
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommitService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;


/**
 * Store of commits
 */
public class CommitStore extends ItemStore {

    private final Map<String, ItemReferences<Commit>> commits = new HashMap<>();

    private final Context context;

    /**
     * Create commit store
     *
     * @param context
     */
    public CommitStore(final Context context) {
        this.context = context;
    }

    /**
     * Get commit
     *
     * @param repo
     * @param id
     * @return commit or null if not in store
     */
    public Commit getCommit(final Repository repo, final String id) {
        final ItemReferences<Commit> repoCommits = commits.get(InfoUtils.createRepoId(repo));
        return repoCommits != null ? repoCommits.get(id) : null;
    }

    /**
     * Add commit to store
     *
     * @param repo
     * @param commit
     * @return commit
     */
    public Commit addCommit(Repository repo, Commit commit) {
        Commit current = getCommit(repo, commit.sha());
        if (current != null && current.equals(commit)) {
            return current;
        }

        String repoId = InfoUtils.createRepoId(repo);
        ItemReferences<Commit> repoCommits = commits.get(repoId);
        if (repoCommits == null) {
            repoCommits = new ItemReferences<>();
            commits.put(repoId, repoCommits);
        }
        repoCommits.put(commit.sha(), commit);
        return commit;
    }

    /**
     * Refresh commit.
     *
     * @param repo The repo which the commit is in
     * @param id The id of the commit
     * @return refreshed commit
     */
    public Single<Commit> refreshCommit(final Repository repo, final String id) {
        return ServiceGenerator.createService(context, RepositoryCommitService.class)
                .getCommit(repo.owner().login(), repo.name(), id)
                .map(response -> addCommit(repo, response.body()));
    }
}
