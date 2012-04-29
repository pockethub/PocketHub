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
package com.github.mobile.core.gist;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

/**
 * Gist model with comments and starred status
 */
public class FullGist extends ArrayList<Comment> implements Serializable {

    /** serialVersionUID */
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
    public FullGist(final Gist gist, final boolean starred, final Collection<Comment> comments) {
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

    /**
     * Get files in gist
     *
     * @return list of files
     */
    public List<GistFile> getFiles() {
        return newArrayList(gist.getFiles().values());
    }
}
