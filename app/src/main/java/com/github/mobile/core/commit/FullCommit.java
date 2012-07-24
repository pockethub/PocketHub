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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.RepositoryCommit;

/**
 * Commit model with comments
 */
public class FullCommit extends ArrayList<CommitComment> implements
        Serializable {

    private static final long serialVersionUID = 2470370479577730822L;

    private final RepositoryCommit commit;

    /**
     * Create commit with comments
     *
     * @param commit
     * @param comments
     */
    public FullCommit(final RepositoryCommit commit,
            final Collection<CommitComment> comments) {
        super(comments);

        this.commit = commit;
    }

    /**
     * Create empty full commit
     */
    public FullCommit() {
        this.commit = null;
    }

    /**
     * @return commit
     */
    public RepositoryCommit getCommit() {
        return commit;
    }
}
