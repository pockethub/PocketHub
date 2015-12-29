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

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.R;
import com.github.pockethub.ui.repo.RepositoryViewActivity;
import com.github.pockethub.ui.roboactivities.RoboAppCompatActivity;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.InfoUtils;
import com.github.pockethub.util.ToastUtils;
import com.google.inject.Inject;

import static android.app.SearchManager.APP_DATA;
import static android.app.SearchManager.QUERY;
import static android.content.Intent.ACTION_SEARCH;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY;

/**
 * Activity to search issues
 */
public class IssueSearchActivity extends RoboAppCompatActivity {

    @Inject
    private AvatarLoader avatars;

    private Repo repository;

    private SearchIssueListFragment issueFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(R.menu.activity_search, options);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = options.findItem(R.id.m_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        Bundle args = new Bundle();
        args.putParcelable(EXTRA_REPOSITORY, repository);
        searchView.setAppSearchData(args);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_clear:
                IssueSearchSuggestionsProvider.clear(this);
                ToastUtils.show(this, R.string.search_history_cleared);
                return true;
            case android.R.id.home:
                Intent intent = RepositoryViewActivity.createIntent(repository);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_issue_search);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        Bundle appData = getIntent().getBundleExtra(APP_DATA);
        if (appData != null) {
            repository = (Repo) appData.getParcelable(EXTRA_REPOSITORY);
            if (repository != null) {
                actionBar.setSubtitle(InfoUtils.createRepoId(repository));
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        avatars.bind(actionBar, repository.owner);

        issueFragment = (SearchIssueListFragment) getSupportFragmentManager()
            .findFragmentById(android.R.id.list);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        issueFragment.setListShown(false);
        handleIntent(intent);
        issueFragment.refresh();
    }

    private void handleIntent(Intent intent) {
        if (ACTION_SEARCH.equals(intent.getAction()))
            search(intent.getStringExtra(QUERY));
    }

    private void search(final String query) {
        getSupportActionBar().setTitle(query);
        IssueSearchSuggestionsProvider.save(this, query);
        issueFragment.setQuery(query);
    }
}
