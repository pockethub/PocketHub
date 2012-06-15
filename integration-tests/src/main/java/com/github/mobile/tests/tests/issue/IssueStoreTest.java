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
package com.github.mobile.tests.tests.issue;

import android.test.AndroidTestCase;

import com.github.mobile.core.issue.IssueStore;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Unit tests of {@link IssueStore}
 */
public class IssueStoreTest extends AndroidTestCase {

    /**
     * Verify issue is updated when re-added
     */
    public void testReuseIssue() {
        IssueStore store = new IssueStore(new IssueService());
        Repository repo = new Repository();
        repo.setName("name");
        repo.setOwner(new User().setLogin("owner"));

        assertNull(store.getIssue(repo, 1));

        RepositoryIssue issue = new RepositoryIssue();
        issue.setRepository(repo).setNumber(1).setBody("body");
        assertSame(issue, store.addIssue(issue));
        assertSame(issue, store.getIssue(repo, 1));

        RepositoryIssue issue2 = new RepositoryIssue();
        issue2.setRepository(repo).setNumber(1).setBody("body2");
        assertSame(issue, store.addIssue(issue2));
        assertEquals(issue2.getBody(), issue.getBody());
        assertSame(issue, store.getIssue(repo, 1));
    }
}
