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
package com.github.mobile.ui;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.R.id;
import com.viewpagerindicator.R.layout;

/**
 * Actiivty with tabbed pages
 *
 * @param <V>
 */
public abstract class TabPagerActivity<V extends PagerAdapter> extends
        DialogFragmentActivity {

    /**
     * View pager
     */
    protected ViewPager pager;

    /**
     * Tab host
     */
    protected TabHost host;

    /**
     * Pager adapter
     */
    protected V adapter;

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
     * Set current item to new position
     * <p>
     * This is guaranteed to only be called when a position changes and the
     * current item of the pager has already been updated to the given position
     * <p>
     * Sub-classes may override this method
     *
     * @param position
     */
    protected void setCurrentItem(final int position) {
        // Intentionally left blank
    }

    /**
     * Get content view to be used when {@link #onCreate(Bundle)} is called
     *
     * @return layout resource id
     */
    protected int getContentView() {
        return layout.pager_with_tabs;
    }

    private void updateCurrentItem(final int newPosition) {
        int currentItem = pager.getCurrentItem();
        if (newPosition > -1 && newPosition < adapter.getCount()
                && currentItem != newPosition) {
            pager.setCurrentItem(newPosition);
            setCurrentItem(newPosition);
        }
    }

    private void createPager() {
        pager = (ViewPager) findViewById(id.vp_pages);
        pager.setOnPageChangeListener(new OnPageChangeListener() {

            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
                // Intentionally left blank
            }

            public void onPageSelected(final int position) {
                host.setCurrentTab(position);
            }

            public void onPageScrollStateChanged(int state) {
                // Intentionally left blank
            }
        });
        adapter = createAdapter();
        pager.setAdapter(adapter);
    }

    private void createTabs() {
        host = (TabHost) findViewById(id.th_tabs);
        host.setup();
        host.setOnTabChangedListener(new OnTabChangeListener() {

            public void onTabChanged(String tabId) {
                updateCurrentItem(host.getCurrentTab());
            }
        });
        LayoutInflater inflater = getLayoutInflater();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            TabSpec spec = host.newTabSpec("tab" + i);
            spec.setContent(new TabContentFactory() {

                @Override
                public View createTabContent(String tag) {
                    return ViewUtils.setGone(new View(TabPagerActivity.this),
                            true);
                }
            });
            View view = inflater.inflate(layout.tab, null);
            ((TextView) view.findViewById(id.tv_tab)).setText(getTitle(i));
            spec.setIndicator(view);
            host.addTab(spec);
        }
    }

    /**
     * Create tabs and pager
     */
    protected void createTabPager() {
        createPager();
        createTabs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentView());
    }
}
