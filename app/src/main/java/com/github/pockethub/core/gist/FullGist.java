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
package com.github.pockethub.core.gist;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GithubComment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Gist model with comments and starred status
 */
public class FullGist extends ArrayList<GithubComment> implements Serializable {

    private static final long serialVersionUID = -5966699489498437000L;

    private final Gist gist;

    private final boolean starred;

    /**
     * Create gist with comments
     *
     * @param gist
     * @param starred
     * @param comments
     */
    public FullGist(final Gist gist, final boolean starred,
            final Collection<GithubComment> comments) {
        super(comments);

        this.starred = starred;
        this.gist = gist;
    }

    /**
     * Create empty gist
     */
    public FullGist() {
        this.gist = null;
        this.starred = false;
    }

    /**
     * @return starred
     */
    public boolean isStarred() {
        return starred;
    }

    /**
     * @return gist
     */
    public Gist getGist() {
        return gist;
    }
}
