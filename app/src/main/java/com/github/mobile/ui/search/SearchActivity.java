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
package com.github.mobile.ui.search;

import static android.app.SearchManager.QUERY;
import static android.content.Intent.ACTION_SEARCH;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.util.TypefaceUtils.ICON_PERSON;
import static com.github.mobile.util.TypefaceUtils.ICON_PUBLIC;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.ui.TabPagerActivity;
import com.github.mobile.ui.user.HomeActivity;
import com.github.mobile.util.ToastUtils;

/**
 * Activity to view search results
 */
public class SearchActivity extends TabPagerActivity<SearchPagerAdapter> {

    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingBar = finder.find(id.pb_loading);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.search, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case id.m_search:
                onSearchRequested();
                return true;
            case id.m_clear:
                RepositorySearchSuggestionsProvider.clear(this);
                ToastUtils.show(this, string.search_history_cleared);
                return true;
            case android.R.id.home:
                Intent intent = new Intent(this, HomeActivity.class);
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
        return layout.tabbed_progress_pager;
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
        configurePager();
    }

    private void configurePager() {
        configureTabPager();
        ViewUtils.setGone(loadingBar, true);
        setGone(false);
    }
}
