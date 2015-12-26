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
package com.github.pockethub.tests.commit;

import android.test.AndroidTestCase;

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.CommitFile;
import com.alorma.github.sdk.bean.dto.response.GitCommit;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.core.commit.CommitUtils;
import com.github.pockethub.util.TimeUtils;

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
        GitCommit gitCommit = new GitCommit();
        gitCommit.sha = "abc";
        assertEquals("abc", CommitUtils.abbreviate(gitCommit));
        Commit commit = new Commit();
        commit.sha = "abcd";
        assertEquals("abcd",
                CommitUtils.abbreviate(commit.sha));
    }

    /**
     * Test commit name parsing from path
     */
    public void testGetName() {
        assertNull(CommitUtils.getName((String) null));
        assertNull(CommitUtils.getName((CommitFile) null));
        assertEquals("", CommitUtils.getName(""));
        assertEquals("/", CommitUtils.getName("/"));
        assertEquals("b", CommitUtils.getName("a/b"));
        CommitFile file = new CommitFile();
        file.filename = "a/b/c";
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
        Commit commit = new Commit();
        assertNull(CommitUtils.getAuthor(commit));
        GitCommit rawCommit = new GitCommit();
        commit.commit = rawCommit;
        assertNull(CommitUtils.getAuthor(commit));
        User user = new User();
        rawCommit.author = user;
        assertNull(CommitUtils.getAuthor(commit));
        user.login = "u1";
        assertEquals("u1", CommitUtils.getAuthor(commit));
    }

    /**
     * Test parsing committer from commit
     */
    public void testGetCommitter() {
        Commit commit = new Commit();
        assertNull(CommitUtils.getCommitter(commit));
        GitCommit rawCommit = new GitCommit();
        commit.commit = rawCommit;
        assertNull(CommitUtils.getCommitter(commit));
        User user = new User();
        rawCommit.committer = user;
        assertNull(CommitUtils.getCommitter(commit));
        user.login = "u1";
        assertEquals("u1", CommitUtils.getCommitter(commit));
        user.login = "u2";
        commit.committer = user;
        assertEquals("u2", CommitUtils.getCommitter(commit));
    }

    /**
     * Test parsing author date from commit
     */
    public void testGetAuthorDate() {
        Commit commit = new Commit();
        assertNull(CommitUtils.getAuthorDate(commit));
        GitCommit rawCommit = new GitCommit();
        commit.commit = rawCommit;
        assertNull(CommitUtils.getAuthorDate(commit));
        User user = new User();
        rawCommit.author = user;
        assertNull(CommitUtils.getAuthorDate(commit));
        user.date = TimeUtils.dateToString(new Date(12000));
        assertEquals(new Date(12000), CommitUtils.getAuthorDate(commit));
    }

    /**
     * Test parsing committer date from commit
     */
    public void testGetCommitterDate() {
        Commit commit = new Commit();
        assertNull(CommitUtils.getCommitterDate(commit));
        GitCommit rawCommit = new GitCommit();
        commit.commit = rawCommit;
        assertNull(CommitUtils.getCommitterDate(commit));
        User user = new User();
        rawCommit.committer = user;
        assertNull(CommitUtils.getCommitterDate(commit));
        user.date = TimeUtils.dateToString(new Date(12000));
        assertEquals(new Date(12000), CommitUtils.getCommitterDate(commit));
    }
}
