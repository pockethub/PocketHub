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

import android.test.AndroidTestCase;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.core.issue.IssueStore;
import com.github.pockethub.util.InfoUtils;

/**
 * Unit tests of {@link IssueStore}
 */
public class IssueStoreTest extends AndroidTestCase {

    /**
     * Verify issue is updated when re-added
     */
    public void testReuseIssue() {
        IssueStore store = new IssueStore(mContext);
        Repo repo = InfoUtils.createRepoFromData("owner", "name");

        assertNull(store.getIssue(repo, 1));

        Issue issue = new Issue();
        issue.repository = repo;
        issue.number = 1;
        issue.body = "body";
        assertSame(issue, store.addIssue(issue));
        assertSame(issue, store.getIssue(repo, 1));

        Issue issue2 = new Issue();
        issue2.repository = repo;
        issue2.number = 1;
        issue2.body = "body2";
        assertSame(issue, store.addIssue(issue2));
        assertEquals(issue2.body, issue.body);
        assertSame(issue, store.getIssue(repo, 1));
    }
}
