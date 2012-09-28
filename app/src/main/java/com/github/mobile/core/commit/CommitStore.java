/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.core.commit;

import com.github.mobile.core.ItemStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.service.CommitService;

/**
 * Store of commits
 */
public class CommitStore extends ItemStore {

    private final Map<String, ItemReferences<RepositoryCommit>> commits = new HashMap<String, ItemReferences<RepositoryCommit>>();

    private final CommitService service;

    /**
     * Create commit store
     *
     * @param service
     */
    public CommitStore(final CommitService service) {
        this.service = service;
    }

    /**
     * Get commit
     *
     * @param repo
     * @param id
     * @return commit or null if not in store
     */
    public RepositoryCommit getCommit(final IRepositoryIdProvider repo,
            final String id) {
        final ItemReferences<RepositoryCommit> repoCommits = commits.get(repo
                .generateId());
        return repoCommits != null ? repoCommits.get(id) : null;
    }

    /**
     * Add commit to store
     *
     * @param repo
     * @param commit
     * @return commit
     */
    public RepositoryCommit addCommit(IRepositoryIdProvider repo,
            RepositoryCommit commit) {
        RepositoryCommit current = getCommit(repo, commit.getSha());
        if (current != null) {
            current.setAuthor(commit.getAuthor());
            current.setCommit(commit.getCommit());
            current.setCommitter(commit.getCommitter());
            current.setFiles(commit.getFiles());
            current.setParents(commit.getParents());
            current.setSha(commit.getSha());
            current.setStats(commit.getStats());
            current.setUrl(commit.getUrl());
            return current;
        } else {
            String repoId = repo.generateId();
            ItemReferences<RepositoryCommit> repoCommits = commits.get(repoId);
            if (repoCommits == null) {
                repoCommits = new ItemReferences<RepositoryCommit>();
                commits.put(repoId, repoCommits);
            }
            repoCommits.put(commit.getSha(), commit);
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
    public RepositoryCommit refreshCommit(final IRepositoryIdProvider repo,
            final String id) throws IOException {
        return addCommit(repo, service.getCommit(repo, id));
    }
}
