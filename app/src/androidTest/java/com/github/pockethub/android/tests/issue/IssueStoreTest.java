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

import android.test.AndroidTestCase;

import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.util.InfoUtils;

import static android.test.MoreAsserts.assertNotEqual;

/**
 * Unit tests of {@link IssueStore}
 */
public class IssueStoreTest extends AndroidTestCase {

    /**
     * Verify issue is updated when re-added
     */
    public void testReuseIssue() {
        IssueStore store = new IssueStore(mContext);
        Repository repo = InfoUtils.createRepoFromData("owner", "name");

        assertNull(store.getIssue(repo, 1));

        Issue issue = Issue.builder()
                .repository(repo)
                .number(1)
                .body("body")
                .build();
        assertEquals(issue, store.addIssue(issue));
        assertEquals(issue, store.getIssue(repo, 1));

        Issue issue2 = Issue.builder()
                .repository(repo)
                .number(1)
                .body("body2")
                .build();

        assertNotEqual(issue, store.addIssue(issue2));
        assertNotEqual(issue2.body(), issue.body());
        assertNotEqual(issue, store.getIssue(repo, 1));
    }
}
