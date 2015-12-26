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

package com.github.pockethub.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.R;

import static android.widget.TabHost.OnTabChangeListener;
import static android.widget.TabHost.TabContentFactory;

public abstract class TabPagerFragment<V extends PagerAdapter & FragmentProvider>
    extends PagerFragment implements OnTabChangeListener, TabContentFactory {


    /**
     * View pager
     */
    protected ViewPager pager;

    /**
     * Tab host
     */
    protected TabLayout slidingTabsLayout;

    /**
     * Pager adapter
     */
    protected V adapter;

    @Override
    public void onPageSelected(final int position) {
        super.onPageSelected(position);
    }

    @Override
    public void onTabChanged(String tabId) {
    }

    @Override
    public View createTabContent(String tag) {
        return ViewUtils.setGone(new View(getActivity().getApplication()), true);
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
    protected TabPagerFragment<V> setGone(boolean gone) {
        ViewUtils.setGone(slidingTabsLayout, gone);
        ViewUtils.setGone(pager, gone);
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

    /**
     * Get content view to be used when {@link #onCreate(android.os.Bundle)} is called
     *
     * @return layout resource id
     */
    protected int getContentView() {
        return R.layout.pager_with_tabs;
    }

    private void createPager() {
        adapter = createAdapter();
        getActivity().supportInvalidateOptionsMenu();
        pager.setAdapter(adapter);
        slidingTabsLayout.setupWithViewPager(pager);
    }

    /**
     * Configure tabs and pager
     */
    protected void configureTabPager() {
        createPager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getContentView(), null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        view.findViewById(R.id.toolbar).setVisibility(View.GONE);

        // On Lollipop, the action bar shadow is provided by default, so have to remove it explicitly
        ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);

        pager = (ViewPager) view.findViewById(R.id.vp_pages);
        pager.setOnPageChangeListener(this);
        slidingTabsLayout = (TabLayout) view.findViewById(R.id.sliding_tabs_layout);
    }

    @Override
    protected FragmentProvider getProvider() {
        return adapter;
    }

}
