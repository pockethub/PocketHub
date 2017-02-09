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

import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.GitHubFile;
import com.github.pockethub.android.core.commit.CommitUtils;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.meisolsson.githubsdk.model.git.GitUser;

import java.util.Date;

/**
 * Test of {@link CommitUtils}
 */
public class CommitUtilsTest extends AndroidTestCase {

    /**
     * Test commit SHA-1 abbreviation
     */
    public void testAbbreviate() {
        assertNull(CommitUtils.abbreviate((GitCommit) null));
        assertNull(CommitUtils.abbreviate((Commit) null));
        assertNull(CommitUtils.abbreviate((String) null));
        assertEquals("", CommitUtils.abbreviate(""));
        assertEquals("a", CommitUtils.abbreviate("a"));
        assertEquals("abcdefghij", CommitUtils.abbreviate("abcdefghijk"));

        GitCommit gitCommit = GitCommit.builder()
                .sha("abc")
                .build();

        assertEquals("abc", CommitUtils.abbreviate(gitCommit));

        Commit commit = Commit.builder()
                .sha("abcd")
                .build();

        assertEquals("abcd", CommitUtils.abbreviate(commit));
    }

    /**
     * Test commit name parsing from path
     */
    public void testGetName() {
        assertNull(CommitUtils.getName((String) null));
        assertNull(CommitUtils.getName((GitHubFile) null));
        assertEquals("", CommitUtils.getName(""));
        assertEquals("/", CommitUtils.getName("/"));
        assertEquals("b", CommitUtils.getName("a/b"));
        GitHubFile file = GitHubFile.builder().filename("a/b/c").build();
        assertEquals("c", CommitUtils.getName(file));
    }

    /**
     * Test commit SHA-1 evaluation
     */
    public void testIsValidCommit() {
        assertFalse(CommitUtils.isValidCommit(null));
        assertFalse(CommitUtils.isValidCommit(""));
        assertTrue(CommitUtils.isValidCommit("a"));
        assertTrue(CommitUtils.isValidCommit("bbbbb"));
        assertFalse(CommitUtils.isValidCommit("am"));
        assertFalse(CommitUtils.isValidCommit("xyz"));
    }

    /**
     * Test parsing author from commit
     */
    public void testGetAuthor() {
        Commit commit = Commit.builder().build();
        assertNull(CommitUtils.getAuthor(commit));

        GitCommit rawCommit = GitCommit.builder().build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertNull(CommitUtils.getAuthor(commit));

        GitUser user = GitUser.builder().build();
        rawCommit = rawCommit.toBuilder()
                .author(user)
                .build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertNull(CommitUtils.getAuthor(commit));

        user = user.toBuilder().name("u1").build();
        rawCommit = rawCommit.toBuilder()
                .author(user)
                .build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertEquals("u1", CommitUtils.getAuthor(commit));
    }

    /**
     * Test parsing committer from commit
     */
    public void testGetCommitter() {
        Commit commit = Commit.builder().build();
        assertNull(CommitUtils.getCommitter(commit));

        GitCommit rawCommit = GitCommit.builder().build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertNull(CommitUtils.getCommitter(commit));

        GitUser user = GitUser.builder().build();
        rawCommit = rawCommit.toBuilder()
                .committer(user)
                .build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertNull(CommitUtils.getCommitter(commit));

        user = user.toBuilder().name("u1").build();
        rawCommit = rawCommit.toBuilder()
                .committer(user)
                .build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertEquals("u1", CommitUtils.getCommitter(commit));

        user = user.toBuilder().name("u2").build();
        rawCommit = rawCommit.toBuilder()
                .committer(user)
                .build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertEquals("u2", CommitUtils.getCommitter(commit));
    }

    /**
     * Test parsing author date from commit
     */
    public void testGetAuthorDate() {
        Commit commit = Commit.builder().build();
        assertNull(CommitUtils.getAuthorDate(commit));

        GitCommit rawCommit = GitCommit.builder().build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertNull(CommitUtils.getAuthorDate(commit));

        GitUser user = GitUser.builder().build();
        rawCommit = rawCommit.toBuilder()
                .author(user)
                .build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertNull(CommitUtils.getAuthorDate(commit));

        user = user.toBuilder()
                .date(new Date(12000))
                .build();
        rawCommit = rawCommit.toBuilder()
                .author(user)
                .build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertEquals(new Date(12000), CommitUtils.getAuthorDate(commit));
    }

    /**
     * Test parsing committer date from commit
     */
    public void testGetCommitterDate() {
        Commit commit = Commit.builder().build();
        assertNull(CommitUtils.getCommitterDate(commit));

        GitCommit rawCommit = GitCommit.builder().build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertNull(CommitUtils.getCommitterDate(commit));

        GitUser user = GitUser.builder().build();
        rawCommit = rawCommit.toBuilder()
                .committer(user)
                .build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertNull(CommitUtils.getCommitterDate(commit));

        user = user.toBuilder()
                .date(new Date(12000))
                .build();
        rawCommit = rawCommit.toBuilder()
                .committer(user)
                .build();
        commit = commit.toBuilder()
                .commit(rawCommit)
                .build();
        assertEquals(new Date(12000), CommitUtils.getCommitterDate(commit));
    }
}
