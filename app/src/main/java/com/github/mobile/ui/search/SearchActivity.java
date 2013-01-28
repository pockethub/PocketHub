package com.github.mobile.ui.search;

import com.actionbarsherlock.view.Menu;
import com.github.mobile.R.menu;
import com.github.mobile.ui.TabPagerActivity;

/**
 * Activity to view search results
 */
public class SearchActivity extends TabPagerActivity<SearchPagerAdapter> {

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.search, options);
        return true;
    }

    @Override
    protected SearchPagerAdapter createAdapter() {
        return new SearchPagerAdapter(this);
    }
}
