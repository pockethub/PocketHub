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
package com.github.pockethub.android.tests.commit;

import android.test.AndroidTestCase;

import com.github.pockethub.android.core.commit.FullCommit;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.GitHubFile;
import com.meisolsson.githubsdk.model.git.GitComment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Tests of {@link FullCommit}
 */
public class FullCommitTest extends AndroidTestCase {

    /**
     * Test commit with one file and one line comment
     */
    public void testSingleLineCommentSingleFile() {
        GitHubFile file = GitHubFile.builder()
                .filename("a.txt")
                .build();

        GitComment comment = GitComment.builder()
                .path(file.filename())
                .position(10)
                .build();

        Commit commit = Commit.builder()
                .files(Collections.singletonList(file))
                .build();

        FullCommit full = new FullCommit(commit, new ArrayList<>(
                Collections.singletonList(comment)));

        assertTrue(full.isEmpty());
        assertEquals(1, full.getFiles().size());
        assertEquals(comment, full.getFiles().get(0).get(10).get(0));
    }

    /**
     * Test commit with one file and one commit comment
     */
    public void testSingleCommentSingleFile() {
        GitHubFile file = GitHubFile.builder()
                .filename("a.txt")
                .build();

        GitComment comment = GitComment.builder().build();

        Commit commit = Commit.builder()
                .files(Collections.singletonList(file))
                .build();

        FullCommit full = new FullCommit(commit, Collections.singletonList(comment));
        assertFalse(full.isEmpty());
        assertEquals(comment, full.get(0));
        assertEquals(1, full.getFiles().size());
    }

    /**
     * Test commit with no files and one commit comment
     */
    public void testSingleCommentNoFiles() {
        GitComment comment = GitComment.builder().build();

        Commit commit = Commit.builder().build();

        FullCommit full = new FullCommit(commit, Collections.singletonList(comment));
        assertFalse(full.isEmpty());
        assertEquals(comment, full.get(0));
        assertTrue(full.getFiles().isEmpty());
    }

    /**
     * Test commit with no comments and one file
     */
    public void testNoCommentsSingleFile() {
        GitHubFile file = GitHubFile.builder()
                .filename("a.txt")
                .build();

        Commit commit = Commit.builder()
                .files(Collections.singletonList(file))
                .build();

        FullCommit full = new FullCommit(commit);
        assertTrue(full.isEmpty());
        assertEquals(1, full.getFiles().size());
    }

    /**
     * Test commit with line and global comments
     */
    public void testBothTypesOfComments() {
        GitHubFile file = GitHubFile.builder()
                .filename("a.txt")
                .build();

        GitComment comment1 = GitComment.builder()
                .path(file.filename())
                .position(10)
                .build();

        GitComment comment2 = GitComment.builder().build();

        Commit commit = Commit.builder()
                .files(Collections.singletonList(file))
                .build();

        FullCommit full = new FullCommit(commit, new ArrayList<>(Arrays.asList(comment1, comment2)));
        assertEquals(1, full.size());
        assertEquals(comment2, full.get(0));
        assertEquals(1, full.getFiles().size());
        assertEquals(comment1, full.getFiles().get(0).get(10).get(0));
    }
}
