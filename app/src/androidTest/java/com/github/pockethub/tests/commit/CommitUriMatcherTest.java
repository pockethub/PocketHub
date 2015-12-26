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

import android.net.Uri;
import android.test.AndroidTestCase;

import com.github.pockethub.core.commit.CommitMatch;
import com.github.pockethub.core.commit.CommitUriMatcher;

/**
 * Tests of {@link CommitUriMatcher}
 */
public class CommitUriMatcherTest extends AndroidTestCase {

    /**
     * Verity empty uri
     */
    public void testEmptyUri() {
        assertNull(CommitUriMatcher.getCommit(Uri.parse("")));
    }

    /**
     * Verify non-hex commit SHA-1 in uri
     */
    public void testNonHexId() {
        assertNull(CommitUriMatcher.getCommit(Uri
                .parse("https://github.com/defunkt/resque/commit/abck")));
    }

    /**
     * Verify http uri
     */
    public void testHttpUri() {
        CommitMatch commit = CommitUriMatcher.getCommit(Uri
                .parse("https://github.com/defunkt/resque/commit/abcd"));
        assertNotNull(commit);
        assertEquals("abcd", commit.commit);
        assertNotNull(commit.repository);
        assertEquals("resque", commit.repository.name);
        assertNotNull(commit.repository.owner);
        assertEquals("defunkt", commit.repository.owner.login);
    }

    /**
     * Verify https uri
     */
    public void testHttpsUri() {
        CommitMatch commit = CommitUriMatcher.getCommit(Uri
                .parse("https://github.com/defunkt/resque/commit/1234"));
        assertNotNull(commit);
        assertEquals("1234", commit.commit);
        assertNotNull(commit.repository);
        assertEquals("resque", commit.repository.name);
        assertNotNull(commit.repository.owner);
        assertEquals("defunkt", commit.repository.owner.login);
    }

    /**
     * Verify uri with comment fragment
     */
    public void testCommentUri() {
        CommitMatch commit = CommitUriMatcher
                .getCommit(Uri
                        .parse("https://github.com/defunkt/resque/commit/a1b2#commitcomment-1605701"));
        assertNotNull(commit);
        assertEquals("a1b2", commit.commit);
        assertNotNull(commit.repository);
        assertEquals("resque", commit.repository.name);
        assertNotNull(commit.repository.owner);
        assertEquals("defunkt", commit.repository.owner.login);
    }
}
