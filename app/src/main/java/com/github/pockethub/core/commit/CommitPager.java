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

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.core.ResourcePager;

/**
 * Pager over commits
 */
public abstract class CommitPager extends ResourcePager<Commit> {

    private final Repo repository;

    private final CommitStore store;

    /**
     * Create pager
     *
     * @param repository
     * @param store
     */
    public CommitPager(final Repo repository, final CommitStore store) {
        this.repository = repository;
        this.store = store;
    }

    @Override
    protected Object getId(final Commit resource) {
        return resource.sha;
    }

    @Override
    protected Commit register(final Commit resource) {
        return store.addCommit(repository, resource);
    }
}
