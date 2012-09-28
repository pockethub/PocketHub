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
package com.github.mobile.tests.commit;

import android.test.AndroidTestCase;

import com.github.mobile.core.commit.FullCommit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.RepositoryCommit;

/**
 * Tests of {@link FullCommit}
 */
public class FullCommitTest extends AndroidTestCase {

    /**
     * Test commit with one file and one line comment
     */
    public void testSingleLineCommentSingleFile() {
        RepositoryCommit commit = new RepositoryCommit();
        CommitFile file = new CommitFile();
        file.setFilename("a.txt");
        CommitComment comment = new CommitComment();
        comment.setPath(file.getFilename());
        comment.setPosition(10);
        commit.setFiles(Collections.singletonList(file));
        FullCommit full = new FullCommit(commit, new ArrayList<CommitComment>(
                Collections.singletonList(comment)));
        assertTrue(full.isEmpty());
        assertEquals(1, full.getFiles().size());
        assertEquals(comment, full.getFiles().get(0).get(10).get(0));
    }

    /**
     * Test commit with one file and one commit comment
     */
    public void testSingleCommentSingleFile() {
        RepositoryCommit commit = new RepositoryCommit();
        CommitFile file = new CommitFile();
        file.setFilename("a.txt");
        CommitComment comment = new CommitComment();
        commit.setFiles(Collections.singletonList(file));
        FullCommit full = new FullCommit(commit, new ArrayList<CommitComment>(
                Collections.singletonList(comment)));
        assertFalse(full.isEmpty());
        assertEquals(comment, full.get(0));
        assertEquals(1, full.getFiles().size());
    }

    /**
     * Test commit with no files and one commit comment
     */
    public void testSingleCommentNoFiles() {
        RepositoryCommit commit = new RepositoryCommit();
        CommitComment comment = new CommitComment();
        FullCommit full = new FullCommit(commit, new ArrayList<CommitComment>(
                Collections.singletonList(comment)));
        assertFalse(full.isEmpty());
        assertEquals(comment, full.get(0));
        assertTrue(full.getFiles().isEmpty());
    }

    /**
     * Test commit with no comments and one file
     */
    public void testNoCommentsSingleFile() {
        RepositoryCommit commit = new RepositoryCommit();
        CommitFile file = new CommitFile();
        file.setFilename("a.txt");
        commit.setFiles(Collections.singletonList(file));
        FullCommit full = new FullCommit(commit);
        assertTrue(full.isEmpty());
        assertEquals(1, full.getFiles().size());
    }

    /**
     * Test commit with line and global comments
     */
    public void testBothTypesOfComments() {
        RepositoryCommit commit = new RepositoryCommit();
        CommitFile file = new CommitFile();
        file.setFilename("a.txt");
        commit.setFiles(Collections.singletonList(file));
        CommitComment comment1 = new CommitComment();
        comment1.setPath(file.getFilename());
        comment1.setPosition(10);
        CommitComment comment2 = new CommitComment();
        FullCommit full = new FullCommit(commit, new ArrayList<CommitComment>(
                Arrays.asList(comment1, comment2)));
        assertEquals(1, full.size());
        assertEquals(comment2, full.get(0));
        assertEquals(1, full.getFiles().size());
        assertEquals(comment1, full.getFiles().get(0).get(10).get(0));
    }
}
