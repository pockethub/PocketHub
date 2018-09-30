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
package com.github.pockethub.android.tests.issue;

import android.net.Uri;
import androidx.test.filters.SmallTest;
import com.github.pockethub.android.core.issue.IssueUriMatcher;
import com.meisolsson.githubsdk.model.Issue;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests of {@link IssueUriMatcher}
 */
@SmallTest
public class IssueUriMatcherTest {

    /**
     * Verify empty uri
     */
    @Test
    public void testEmptyUri() {
        assertNull(IssueUriMatcher.getIssue(Uri.parse("")));
    }

    /**
     * Verify non-numeric issue number in uri
     */
    @Test
    public void testNonNumericIssueNumber() {
        assertNull(IssueUriMatcher.getIssue(Uri
                .parse("https://github.com/defunkt/resque/issues/fourty")));
    }

    /**
     * Verify http uri
     */
    @Test
    public void testHttpUri() {
        Issue issue = IssueUriMatcher.getIssue(Uri
                .parse("https://github.com/defunkt/resque/issues/3"));
        assertNotNull(issue);
        assertEquals(3, issue.number().intValue());
        assertNotNull(issue.repository());
        assertEquals("resque", issue.repository().name());
        assertNotNull(issue.repository().owner());
        assertEquals("defunkt", issue.repository().owner().login());
    }

    /**
     * Verify pull uri
     */
    @Test
    public void testPullUri() {
        Issue issue = IssueUriMatcher.getIssue(Uri
                .parse("https://github.com/defunkt/resque/pull/3"));
        assertNotNull(issue);
        assertEquals(3, issue.number().intValue());
        assertNotNull(issue.repository());
        assertEquals("resque", issue.repository().name());
        assertNotNull(issue.repository().owner());
        assertEquals("defunkt", issue.repository().owner().login());
    }

    /**
     * Verify https uri
     */
    @Test
    public void testHttpsUri() {
        Issue issue = IssueUriMatcher.getIssue(Uri
                .parse("http://github.com/defunkt/resque/issues/15"));
        assertNotNull(issue);
        assertEquals(15, issue.number().intValue());
        assertNotNull(issue.repository());
        assertEquals("resque", issue.repository().name());
        assertNotNull(issue.repository().owner());
        assertEquals("defunkt", issue.repository().owner().login());
    }

    /**
     * Verify uri with comment fragment
     */
    @Test
    public void testCommentUri() {
        Issue issue = IssueUriMatcher
                .getIssue(Uri
                        .parse("https://github.com/defunkt/resque/issues/300#issuecomment-123456"));
        assertNotNull(issue);
        assertEquals(300, issue.number().intValue());
        assertNotNull(issue.repository());
        assertEquals("resque", issue.repository().name());
        assertNotNull(issue.repository().owner());
        assertEquals("defunkt", issue.repository().owner().login());
    }
}
