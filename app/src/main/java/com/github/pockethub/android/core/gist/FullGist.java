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
package com.github.pockethub.android.core.gist;

import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GitHubComment;

import java.util.List;

/**
 * Gist model with comments and starred status
 */
public class FullGist {

    private final Gist gist;
    private final boolean starred;
    private final List<GitHubComment> comments;

    /**
     * Create gist with comments
     *
     * @param gist
     * @param starred
     * @param comments
     */
    public FullGist(final Gist gist, final boolean starred, final List<GitHubComment> comments) {
        this.gist = gist;
        this.starred = starred;
        this.comments = comments;
    }

    /**
     * @return gist
     */
    public Gist getGist() {
        return gist;
    }

    /**
     * @return starred
     */
    public boolean isStarred() {
        return starred;
    }

    /**
     * @return comments
     */
    public List<GitHubComment> getComments() {
        return comments;
    }
}
