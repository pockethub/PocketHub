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

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.RepositoryCommit;

/**
 * Commit model with comments
 */
public class FullCommit extends ArrayList<CommitComment> implements
        Serializable {

    private static final long serialVersionUID = 2470370479577730822L;

    private final RepositoryCommit commit;

    private final List<FullCommitFile> files;

    /**
     * Create commit with no comments
     *
     * @param commit
     */
    public FullCommit(final RepositoryCommit commit) {
        this.commit = commit;
        List<CommitFile> rawFiles = commit.getFiles();
        if (rawFiles != null && !rawFiles.isEmpty()) {
            files = new ArrayList<FullCommitFile>(rawFiles.size());
            for (CommitFile file : rawFiles)
                files.add(new FullCommitFile(file));
        } else
            files = Collections.emptyList();
    }

    /**
     * Create commit with comments
     *
     * @param commit
     * @param comments
     */
    public FullCommit(final RepositoryCommit commit,
            final Collection<CommitComment> comments) {
        this.commit = commit;

        List<CommitFile> rawFiles = commit.getFiles();
        if (rawFiles != null) {
            files = new ArrayList<FullCommitFile>(rawFiles.size());
            if (comments != null && !comments.isEmpty()) {
                for (CommitFile file : rawFiles) {
                    Iterator<CommitComment> iterator = comments.iterator();
                    FullCommitFile full = new FullCommitFile(file);
                    while (iterator.hasNext()) {
                        CommitComment comment = iterator.next();
                        if (file.getFilename().equals(comment.getPath())) {
                            full.add(comment);
                            iterator.remove();
                        }
                    }
                    files.add(full);
                }
                addAll(comments);
            } else
                for (CommitFile file : rawFiles)
                    files.add(new FullCommitFile(file));
        } else
            files = Collections.emptyList();
    }

    @Override
    public boolean add(CommitComment comment) {
        String path = comment.getPath();
        if (TextUtils.isEmpty(path))
            return super.add(comment);
        else {
            boolean added = false;
            for (FullCommitFile file : files)
                if (path.equals(file.getFile().getFilename())) {
                    file.add(comment);
                    added = true;
                    break;
                }
            if (!added)
                added = super.add(comment);
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
    public RepositoryCommit getCommit() {
        return commit;
    }
}
