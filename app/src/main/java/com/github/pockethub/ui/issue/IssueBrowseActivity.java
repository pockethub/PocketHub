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
package com.github.pockethub.ui.issue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.Intents.Builder;
import com.github.pockethub.R;
import com.github.pockethub.core.issue.IssueFilter;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.util.AvatarLoader;
import com.google.inject.Inject;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.pockethub.Intents.EXTRA_ISSUE_FILTER;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY;

/**
 * Activity for browsing a list of issues scoped to a single {@link IssueFilter}
 */
public class IssueBrowseActivity extends DialogFragmentActivity {

    /**
     * Create intent to browse the filtered issues
     *
     * @param filter
     * @return intent
     */
    public static Intent createIntent(IssueFilter filter) {
        return new Builder("repo.issues.VIEW").repo(filter.getRepository())
            .add(EXTRA_ISSUE_FILTER, filter).toIntent();
    }

    private Repo repo;

    @Inject
    private AvatarLoader avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repo = getParcelableExtra(EXTRA_REPOSITORY);

        setContentView(R.layout.activity_repo_issue_list);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(repo.name);
        actionBar.setSubtitle(repo.owner.login);
        actionBar.setDisplayHomeAsUpEnabled(true);
        avatars.bind(actionBar, repo.owner);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = FiltersViewActivity.createIntent();
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
