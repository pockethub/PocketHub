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
package com.github.pockethub.android.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;

import com.github.pockethub.android.R;

import butterknife.BindView;

/**
 * Activity with tabbed pages
 *
 * @param <V>
 */
public abstract class TabPagerActivity<V extends PagerAdapter & FragmentProvider>
        extends PagerActivity implements OnTabChangeListener, TabContentFactory {

    /**
     * View pager
     */
    @BindView(R.id.vp_pages)
    protected ViewPager pager;

    /**
     * Tab host
     */
    @BindView(R.id.sliding_tabs_layout)
    protected TabLayout slidingTabsLayout;

    /**
     * Pager adapter
     */
    protected V adapter;

    @Override
    public void onTabChanged(String tabId) {
    }

    @Override
    public View createTabContent(String tag) {
        View view = new View(getApplication());
        view.setVisibility(View.GONE);
        return view;
    }

    /**
     * Create pager adapter
     *
     * @return pager adapter
     */
    protected abstract V createAdapter();

    /**
     * Get title for position
     *
     * @param position
     * @return title
     */
    protected String getTitle(final int position) {
        return adapter.getPageTitle(position).toString();
    }

    /**
     * Get icon for position
     *
     * @param position
     * @return icon
     */
    protected String getIcon(final int position) {
        return null;
    }

    /**
     * Set tab and pager as gone or visible
     *
     * @param gone
     * @return this activity
     */
    protected TabPagerActivity<V> setGone(boolean gone) {
        if (gone) {
            slidingTabsLayout.setVisibility(View.GONE);
            pager.setVisibility(View.GONE);
        } else {
            slidingTabsLayout.setVisibility(View.VISIBLE);
            pager.setVisibility(View.VISIBLE);
        }
        return this;
    }

    /**
     * Set current item to new position
     * <p/>
     * This is guaranteed to only be called when a position changes and the
     * current item of the pager has already been updated to the given position
     * <p/>
     * Sub-classes may override this method
     *
     * @param position
     */
    protected void setCurrentItem(final int position) {
        // Intentionally left blank
    }

    private void updateCurrentItem(final int newPosition) {
        if (newPosition > -1 && newPosition < adapter.getCount()) {
            pager.setItem(newPosition);
            setCurrentItem(newPosition);
        }
    }

    private void createPager() {
        adapter = createAdapter();
        invalidateOptionsMenu();
        pager.setAdapter(adapter);
    }

    public void updateTabs() {
        slidingTabsLayout.setupWithViewPager(pager);
    }

    /**
     * Configure tabs and pager
     */
    protected void configureTabPager() {
        if (adapter == null) {
            createPager();
            updateTabs();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        pager.addOnPageChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pager.removeOnPageChangeListener(this);
    }

    @Override
    protected FragmentProvider getProvider() {
        return adapter;
    }
}
