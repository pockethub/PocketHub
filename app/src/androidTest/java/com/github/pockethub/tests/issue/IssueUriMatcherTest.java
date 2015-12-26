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
package com.github.pockethub.tests.issue;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.github.pockethub.core.issue.IssueUriMatcher;

/**
 * Unit tests of {@link IssueUriMatcher}
 */
public class IssueUriMatcherTest extends AndroidTestCase {

    /**
     * Verify empty uri
     */
    public void testEmptyUri() {
        assertNull(IssueUriMatcher.getIssue(Uri.parse("")));
    }

    /**
     * Verify non-numeric issue number in uri
     */
    public void testNonNumericIssueNumber() {
        assertNull(IssueUriMatcher.getIssue(Uri
                .parse("https://github.com/defunkt/resque/issues/fourty")));
    }

    /**
     * Verify http uri
     */
    public void testHttpUri() {
        Issue issue = IssueUriMatcher.getIssue(Uri
                .parse("https://github.com/defunkt/resque/issues/3"));
        assertNotNull(issue);
        assertEquals(3, issue.number);
        assertNotNull(issue.repository);
        assertEquals("resque", issue.repository.name);
        assertNotNull(issue.repository.owner);
        assertEquals("defunkt", issue.repository.owner.login);
    }

    /**
     * Verify pull uri
     */
    public void testPullUri() {
        Issue issue = IssueUriMatcher.getIssue(Uri
                .parse("https://github.com/defunkt/resque/pull/3"));
        assertNotNull(issue);
        assertEquals(3, issue.number);
        assertNotNull(issue.repository);
        assertEquals("resque", issue.repository.name);
        assertNotNull(issue.repository.owner);
        assertEquals("defunkt", issue.repository.owner.login);
    }

    /**
     * Verify https uri
     */
    public void testHttpsUri() {
        Issue issue = IssueUriMatcher.getIssue(Uri
                .parse("http://github.com/defunkt/resque/issues/15"));
        assertNotNull(issue);
        assertEquals(15, issue.number);
        assertNotNull(issue.repository);
        assertEquals("resque", issue.repository.name);
        assertNotNull(issue.repository.owner);
        assertEquals("defunkt", issue.repository.owner.login);
    }

    /**
     * Verify uri with comment fragment
     */
    public void testCommentUri() {
        Issue issue = IssueUriMatcher
                .getIssue(Uri
                        .parse("https://github.com/defunkt/resque/issues/300#issuecomment-123456"));
        assertNotNull(issue);
        assertEquals(300, issue.number);
        assertNotNull(issue.repository);
        assertEquals("resque", issue.repository.name);
        assertNotNull(issue.repository.owner);
        assertEquals("defunkt", issue.repository.owner.login);
    }
}
