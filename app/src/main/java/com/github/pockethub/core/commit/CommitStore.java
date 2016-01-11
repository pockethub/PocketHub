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
package com.github.pockethub.core.commit;

import android.content.Context;

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.commit.GetSingleCommitClient;
import com.github.pockethub.core.ItemStore;
import com.github.pockethub.util.InfoUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


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
    public Commit getCommit(final Repo repo, final String id) {
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
    public Commit addCommit(Repo repo, Commit commit) {
        Commit current = getCommit(repo, commit.sha);
        if (current != null) {
            current.author = commit.author;
            current.commit = commit.commit;
            current.committer = commit.committer;
            current.files = commit.files;
            current.parents = commit.parents;
            current.sha = commit.sha;
            current.stats = commit.stats;
            current.url = commit.url;
            return current;
        } else {
            String repoId = InfoUtils.createRepoId(repo);
            ItemReferences<Commit> repoCommits = commits.get(repoId);
            if (repoCommits == null) {
                repoCommits = new ItemReferences<>();
                commits.put(repoId, repoCommits);
            }
            repoCommits.put(commit.sha, commit);
            return commit;
        }
    }

    /**
     * Refresh commit
     *
     * @param repo
     * @param id
     * @return refreshed commit
     * @throws IOException
     */
    public Commit refreshCommit(final Repo repo, final String id) throws IOException {
        return addCommit(repo, new GetSingleCommitClient(InfoUtils.createCommitInfo(repo, id))
                .observable().toBlocking().first());
    }
}
