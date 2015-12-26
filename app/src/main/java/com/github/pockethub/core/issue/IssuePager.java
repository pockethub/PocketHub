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
package com.github.pockethub.core.issue;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.github.pockethub.core.ResourcePager;

/**
 * Helper class for showing more and more pages of issues
 */
public abstract class IssuePager extends ResourcePager<Issue> {

    /**
     * Store to add loaded issues to
     */
    protected final IssueStore store;

    /**
     * Create issue pager
     *
     * @param store
     */
    public IssuePager(final IssueStore store) {
        this.store = store;

    }

    @Override
    protected Issue register(Issue resource) {
        return store.addIssue(resource);
    }

    @Override
    protected Object getId(Issue resource) {
        return resource.id;
    }
}
