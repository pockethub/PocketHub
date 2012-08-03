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

import com.github.mobile.core.ResourcePager;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryCommit;

/**
 * Pager over commits
 */
public abstract class CommitPager extends ResourcePager<RepositoryCommit> {

    private final IRepositoryIdProvider repository;

    private final CommitStore store;

    /**
     * Create pager
     *
     * @param repository
     * @param store
     */
    public CommitPager(final IRepositoryIdProvider repository,
            final CommitStore store) {
        this.repository = repository;
        this.store = store;
    }

    @Override
    protected Object getId(final RepositoryCommit resource) {
        return resource.getSha();
    }

    @Override
    protected RepositoryCommit register(final RepositoryCommit resource) {
        return store.addCommit(repository, resource);
    }
}
