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
package com.github.mobile;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.github.mobile.core.issue.IssueUriMatcher;

import org.eclipse.egit.github.core.RepositoryIssue;

/**
 * Unit tests of {@link IssueUriMatcher}
 */
public class IssueUriMatcherTest extends AndroidTestCase {

    /**
     * Verity empty uri
     */
    public void testEmptyUri() {
        assertNull(IssueUriMatcher.getIssue(Uri.parse("")));
    }

    /**
     * Verity non-numeric issue number in uri
     */
    public void testNonNumericIssueNumber() {
        assertNull(IssueUriMatcher.getIssue(Uri
                .parse("https://github.com/defunkt/resque/issues/fourty")));
    }

    /**
     * Verify http uri
     */
    public void testHttpUri() {
        RepositoryIssue issue = IssueUriMatcher.getIssue(Uri
                .parse("https://github.com/defunkt/resque/issues/3"));
        assertNotNull(issue);
        assertEquals(3, issue.getNumber());
        assertNotNull(issue.getRepository());
        assertEquals("resque", issue.getRepository().getName());
        assertNotNull(issue.getRepository().getOwner());
        assertEquals("defunkt", issue.getRepository().getOwner().getLogin());
    }

    /**
     * Verify https uri
     */
    public void testHttpsUri() {
        RepositoryIssue issue = IssueUriMatcher.getIssue(Uri
                .parse("http://github.com/defunkt/resque/issues/15"));
        assertNotNull(issue);
        assertEquals(15, issue.getNumber());
        assertNotNull(issue.getRepository());
        assertEquals("resque", issue.getRepository().getName());
        assertNotNull(issue.getRepository().getOwner());
        assertEquals("defunkt", issue.getRepository().getOwner().getLogin());
    }

    /**
     * Verify uri with comment fragment
     */
    public void testCommentUri() {
        RepositoryIssue issue = IssueUriMatcher
                .getIssue(Uri
                        .parse("https://github.com/defunkt/resque/issues/300#issuecomment-123456"));
        assertNotNull(issue);
        assertEquals(300, issue.getNumber());
        assertNotNull(issue.getRepository());
        assertEquals("resque", issue.getRepository().getName());
        assertNotNull(issue.getRepository().getOwner());
        assertEquals("defunkt", issue.getRepository().getOwner().getLogin());
    }
}
