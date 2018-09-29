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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.pockethub.android.R;

import butterknife.BindView;

import static android.widget.TabHost.OnTabChangeListener;
import static android.widget.TabHost.TabContentFactory;

public abstract class TabPagerFragment<V extends PagerAdapter & FragmentProvider>
    extends PagerFragment implements OnTabChangeListener, TabContentFactory {


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
        View view = new View(getActivity().getApplication());
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
    protected TabPagerFragment<V> setGone(boolean gone) {
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

    /**
     * Get content view to be used when {@link #onCreate(Bundle)} is called
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        view.findViewById(R.id.toolbar).setVisibility(View.GONE);

        pager.addOnPageChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pager.removeOnPageChangeListener(this);
    }

    @Override
    protected FragmentProvider getProvider() {
        return adapter;
    }

}
