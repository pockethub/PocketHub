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
package com.github.pockethub.android.core.commit;

import android.text.TextUtils;

import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.GitHubFile;
import com.meisolsson.githubsdk.model.git.GitComment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Commit model with comments
 */
public class FullCommit extends ArrayList<GitComment> implements
        Serializable {

    private static final long serialVersionUID = 2470370479577730822L;

    private final Commit commit;

    private final List<FullCommitFile> files;

    /**
     * Create commit with no comments
     *
     * @param commit
     */
    public FullCommit(final Commit commit) {
        this.commit = commit;
        List<GitHubFile> rawFiles = commit.files();
        if (rawFiles != null && !rawFiles.isEmpty()) {
            files = new ArrayList<>(rawFiles.size());
            for (GitHubFile file : rawFiles) {
                files.add(new FullCommitFile(file));
            }
        } else {
            files = Collections.emptyList();
        }
    }

    /**
     * Create commit with comments
     *
     * @param commit
     * @param comments
     */
    public FullCommit(final Commit commit,
            final Collection<GitComment> comments) {
        this.commit = commit;

        List<GitHubFile> rawFiles = commit.files();
        boolean hasComments = comments != null && !comments.isEmpty();
        boolean hasFiles = rawFiles != null && !rawFiles.isEmpty();
        if (hasFiles) {
            files = new ArrayList<>(rawFiles.size());
            if (hasComments) {
                for (GitHubFile file : rawFiles) {
                    Iterator<GitComment> iterator = comments.iterator();
                    FullCommitFile full = new FullCommitFile(file);
                    while (iterator.hasNext()) {
                        GitComment comment = iterator.next();
                        if (file.filename().equals(comment.path())) {
                            full.add(comment);
                            iterator.remove();
                        }
                    }
                    files.add(full);
                }
                hasComments = !comments.isEmpty();
            } else {
                for (GitHubFile file : rawFiles) {
                    files.add(new FullCommitFile(file));
                }
            }
        } else {
            files = Collections.emptyList();
        }

        if (hasComments) {
            addAll(comments);
        }
    }

    @Override
    public boolean add(final GitComment comment) {
        String path = comment.path();
        if (TextUtils.isEmpty(path)) {
            return super.add(comment);
        } else {
            boolean added = false;
            for (FullCommitFile file : files) {
                if (path.equals(file.getFile().filename())) {
                    file.add(comment);
                    added = true;
                    break;
                }
            }
            if (!added) {
                added = super.add(comment);
            }
            return added;
        }
    }

    /**
     * @return files
     */
    public List<FullCommitFile> getFiles() {
        return files;
    }

    /**
     * @return commit
     */
    public Commit getCommit() {
        return commit;
    }
}
