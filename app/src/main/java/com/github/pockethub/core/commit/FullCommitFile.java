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

import android.util.SparseArray;

import com.alorma.github.sdk.bean.dto.response.CommitComment;
import com.alorma.github.sdk.bean.dto.response.CommitFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Commit file with comments
 */
public class FullCommitFile {

    private final SparseArray<List<CommitComment>> comments = new SparseArray<>(
            4);

    private final CommitFile file;

    /**
     * Create file
     *
     * @param file
     */
    public FullCommitFile(final CommitFile file) {
        this.file = file;
    }

    /**
     * Get comments for line
     *
     * @param line
     * @return comments
     */
    public List<CommitComment> get(final int line) {
        List<CommitComment> lineComments = comments.get(line);
        return lineComments != null ? lineComments : Collections
                .<CommitComment> emptyList();
    }

    /**
     * Add comment to file
     *
     * @param comment
     * @return this file
     */
    public FullCommitFile add(final CommitComment comment) {
        int line = comment.position;
        if (line >= 0) {
            List<CommitComment> lineComments = comments.get(line);
            if (lineComments == null) {
                lineComments = new ArrayList<>(4);
                comments.put(line, lineComments);
            }
            lineComments.add(comment);
        }
        return this;
    }

    /**
     * @return file
     */
    public CommitFile getFile() {
        return file;
    }
}
