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

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.core.issue.IssueFilter;
import com.github.pockethub.tests.ActivityTest;
import com.github.pockethub.ui.issue.EditIssuesFilterActivity;
import com.github.pockethub.util.InfoUtils;

/**
 * Tests of {@link EditIssuesFilterActivity}
 */
public class EditIssuesFilterActivityTest extends
    ActivityTest<EditIssuesFilterActivity> {

    /**
     * Create navigation_drawer_header_background
     */
    public EditIssuesFilterActivityTest() {
        super(EditIssuesFilterActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Repo repo = InfoUtils.createRepoFromData("owner", "name");
        IssueFilter filter = new IssueFilter(repo);
        setActivityIntent(EditIssuesFilterActivity.createIntent(filter));
    }
}
