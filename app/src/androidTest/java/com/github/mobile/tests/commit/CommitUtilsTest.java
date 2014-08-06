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

import com.github.mobile.core.commit.CommitUtils;

import java.util.Date;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.User;

/**
 * Test of {@link CommitUtils}
 */
public class CommitUtilsTest extends AndroidTestCase {

    /**
     * Test commit SHA-1 abbreviation
     */
    public void testAbbreviate() {
        assertNull(CommitUtils.abbreviate((Commit) null));
        assertNull(CommitUtils.abbreviate((RepositoryCommit) null));
        assertNull(CommitUtils.abbreviate((String) null));
        assertEquals("", CommitUtils.abbreviate(""));
        assertEquals("a", CommitUtils.abbreviate("a"));
        assertEquals("abcdefghij", CommitUtils.abbreviate("abcdefghijk"));
        assertEquals("abc", CommitUtils.abbreviate(new Commit().setSha("abc")));
        assertEquals("abcd",
                CommitUtils.abbreviate(new RepositoryCommit().setSha("abcd")));
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
        assertEquals("c",
                CommitUtils.getName(new CommitFile().setFilename("a/b/c")));
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
        RepositoryCommit commit = new RepositoryCommit();
        assertNull(CommitUtils.getAuthor(commit));
        Commit rawCommit = new Commit();
        commit.setCommit(rawCommit);
        assertNull(CommitUtils.getAuthor(commit));
        CommitUser user = new CommitUser();
        rawCommit.setAuthor(user);
        assertNull(CommitUtils.getAuthor(commit));
        user.setName("u1");
        assertEquals("u1", CommitUtils.getAuthor(commit));
        commit.setAuthor(new User().setLogin("u2"));
        assertEquals("u2", CommitUtils.getAuthor(commit));
    }

    /**
     * Test parsing committer from commit
     */
    public void testGetCommitter() {
        RepositoryCommit commit = new RepositoryCommit();
        assertNull(CommitUtils.getCommitter(commit));
        Commit rawCommit = new Commit();
        commit.setCommit(rawCommit);
        assertNull(CommitUtils.getCommitter(commit));
        CommitUser user = new CommitUser();
        rawCommit.setCommitter(user);
        assertNull(CommitUtils.getCommitter(commit));
        user.setName("u1");
        assertEquals("u1", CommitUtils.getCommitter(commit));
        commit.setCommitter(new User().setLogin("u2"));
        assertEquals("u2", CommitUtils.getCommitter(commit));
    }

    /**
     * Test parsing author date from commit
     */
    public void testGetAuthorDate() {
        RepositoryCommit commit = new RepositoryCommit();
        assertNull(CommitUtils.getAuthorDate(commit));
        Commit rawCommit = new Commit();
        commit.setCommit(rawCommit);
        assertNull(CommitUtils.getAuthorDate(commit));
        CommitUser user = new CommitUser();
        rawCommit.setAuthor(user);
        assertNull(CommitUtils.getAuthorDate(commit));
        user.setDate(new Date(12345));
        assertEquals(new Date(12345), CommitUtils.getAuthorDate(commit));
    }

    /**
     * Test parsing committer date from commit
     */
    public void testGetCommitterDate() {
        RepositoryCommit commit = new RepositoryCommit();
        assertNull(CommitUtils.getCommitterDate(commit));
        Commit rawCommit = new Commit();
        commit.setCommit(rawCommit);
        assertNull(CommitUtils.getCommitterDate(commit));
        CommitUser user = new CommitUser();
        rawCommit.setCommitter(user);
        assertNull(CommitUtils.getCommitterDate(commit));
        user.setDate(new Date(12345));
        assertEquals(new Date(12345), CommitUtils.getCommitterDate(commit));
    }
}
