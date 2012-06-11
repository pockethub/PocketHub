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
package com.github.mobile.ui.issue;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.Intents.EXTRA_ISSUE_FILTER;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.layout;
import com.github.mobile.core.issue.IssueFilter;
import com.github.mobile.util.AvatarLoader;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Repository;

import roboguice.inject.InjectExtra;

/**
 * Activity for browsing a list of issues scoped to a single {@link IssueFilter}
 */
public class IssueBrowseActivity extends RoboSherlockFragmentActivity {

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

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repo;

    @Inject
    private AvatarLoader avatarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.repo_issue_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(repo.getName());
        actionBar.setSubtitle(repo.getOwner().getLogin());
        actionBar.setDisplayHomeAsUpEnabled(true);
        avatarHelper.bind(actionBar, repo.getOwner());
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
