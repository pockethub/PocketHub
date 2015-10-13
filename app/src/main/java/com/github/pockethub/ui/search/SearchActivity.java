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
package com.github.pockethub.ui.search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.R;
import com.github.pockethub.ui.MainActivity;
import com.github.pockethub.ui.TabPagerActivity;
import com.github.pockethub.util.ToastUtils;

import static android.app.SearchManager.QUERY;
import static android.content.Intent.ACTION_SEARCH;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.pockethub.util.TypefaceUtils.ICON_PERSON;
import static com.github.pockethub.util.TypefaceUtils.ICON_PUBLIC;

/**
 * Activity to view search results
 */
public class SearchActivity extends TabPagerActivity<SearchPagerAdapter> {

    private ProgressBar loadingBar;

    private SearchRepositoryListFragment repoFragment;

    private SearchUserListFragment userFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingBar = finder.find(R.id.pb_loading);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        configurePager();
        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(R.menu.activity_search, options);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = options.findItem(R.id.m_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_clear:
                RepositorySearchSuggestionsProvider.clear(this);
                ToastUtils.show(this, R.string.search_history_cleared);
                return true;
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected SearchPagerAdapter createAdapter() {
        return new SearchPagerAdapter(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.tabbed_progress_pager;
    }

    @Override
    protected String getIcon(int position) {
        switch (position) {
            case 0:
                return ICON_PUBLIC;
            case 1:
                return ICON_PERSON;
            default:
                return super.getIcon(position);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (ACTION_SEARCH.equals(intent.getAction()))
            search(intent.getStringExtra(QUERY));
    }

    private void search(final String query) {
        getSupportActionBar().setTitle(query);
        RepositorySearchSuggestionsProvider.save(this, query);

        findFragments();

        if (repoFragment != null && userFragment != null) {
            repoFragment.setListShown(false);
            userFragment.setListShown(false);

            repoFragment.refresh();
            userFragment.refresh();
        }
    }

    private void configurePager() {
        configureTabPager();
        ViewUtils.setGone(loadingBar, true);
        setGone(false);
    }

    private void findFragments() {
        if (repoFragment == null || userFragment == null) {
            FragmentManager fm = getSupportFragmentManager();
            repoFragment = (SearchRepositoryListFragment) fm.findFragmentByTag(
                "android:switcher:" + pager.getId() + ":" + 0);
            userFragment = (SearchUserListFragment) fm.findFragmentByTag(
                "android:switcher:" + pager.getId() + ":" + 1);
        }
    }
}
