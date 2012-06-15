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
package com.github.mobile.tests.tests.repo;

import static android.app.SearchManager.QUERY;
import static android.content.Intent.ACTION_SEARCH;
import android.content.Intent;

import com.github.mobile.tests.ActivityTest;
import com.github.mobile.ui.repo.RepositorySearchActivity;

/**
 * Tests of {@link RepositorySearchActivity}
 */
public class RepositorySearchActivityTest extends
        ActivityTest<RepositorySearchActivity> {

    /**
     * Create test
     */
    public RepositorySearchActivityTest() {
        super(RepositorySearchActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityIntent(new Intent(ACTION_SEARCH).putExtra(QUERY, "test"));
    }
}
