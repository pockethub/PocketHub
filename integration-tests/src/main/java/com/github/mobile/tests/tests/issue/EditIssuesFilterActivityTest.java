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

import com.github.mobile.core.issue.IssueFilter;
import com.github.mobile.tests.ActivityTest;
import com.github.mobile.ui.issue.EditIssuesFilterActivity;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Tests of {@link EditIssuesFilterActivity}
 */
public class EditIssuesFilterActivityTest extends
        ActivityTest<EditIssuesFilterActivity> {

    /**
     * Create test
     */
    public EditIssuesFilterActivityTest() {
        super(EditIssuesFilterActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Repository repo = new Repository();
        repo.setName("name");
        repo.setOwner(new User().setLogin("owner"));
        IssueFilter filter = new IssueFilter(repo);
        setActivityIntent(EditIssuesFilterActivity.createIntent(filter));
    }
}
