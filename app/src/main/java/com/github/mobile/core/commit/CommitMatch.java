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

import org.eclipse.egit.github.core.Repository;

/**
 * Match for a commit in a repository
 */
public class CommitMatch {

    /**
     * Repository of commit
     */
    public final Repository repository;

    /**
     * SHA-1 of commit
     */
    public final String commit;

    /**
     * Create match
     *
     * @param repository
     * @param commit
     */
    public CommitMatch(final Repository repository, final String commit) {
        this.repository = repository;
        this.commit = commit;
    }
}
