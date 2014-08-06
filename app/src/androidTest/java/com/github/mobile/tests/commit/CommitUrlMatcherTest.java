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

import com.github.mobile.core.commit.CommitMatch;
import com.github.mobile.core.commit.CommitUrlMatcher;

/**
 * Unit tests of {@link CommitUrlMatcher}
 */
public class CommitUrlMatcherTest extends AndroidTestCase {

    /**
     * Check non-matching URLs
     */
    public void testUrlsWithoutCommit() {
        CommitUrlMatcher matcher = new CommitUrlMatcher();

        assertNull(matcher.getCommit(null));
        assertNull(matcher.getCommit(""));
        assertNull(matcher.getCommit(" "));
        assertNull(matcher.getCommit("http://github.com/a/b"));
        assertNull(matcher.getCommit("http://github.com/a/b/commit"));
        assertNull(matcher.getCommit("http://github.com/a/b/commit/@"));
    }

    /**
     * Verify issue URL matching provides commits
     */
    public void testUrlsWithCommit() {
        CommitUrlMatcher matcher = new CommitUrlMatcher();

        CommitMatch match = matcher.getCommit("http://github.com/a/b/commit/1");
        assertNotNull(match);
        assertEquals("1", match.commit);
        assertEquals("a", match.repository.getOwner().getLogin());
        assertEquals("b", match.repository.getName());

        match = matcher.getCommit("https://github.com/a1/b2/commit/abc");
        assertNotNull(match);
        assertEquals("abc", match.commit);
        assertEquals("a1", match.repository.getOwner().getLogin());
        assertEquals("b2", match.repository.getName());
    }
}
