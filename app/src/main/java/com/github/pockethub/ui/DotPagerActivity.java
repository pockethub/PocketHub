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
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.Toolbar;

import com.github.pockethub.R;
import com.github.pockethub.ui.view.DotPageIndicator;

public abstract class DotPagerActivity<V extends PagerAdapter>
        extends PagerActivity{

    private ViewPager pager;
    protected DotPageIndicator dotPageIndicator;

    /**
     * Pager adapter
     */
    protected V adapter;

    @Override
    public void onPageSelected(final int position) {
        super.onPageSelected(position);
    }

    /**
     * Create pager adapter
     *
     * @return pager adapter
     */
    protected abstract V createAdapter();


    /**
     * Creates the adapter and passes it to the {@link ViewPager}
     */
    private void createPager() {
        adapter = createAdapter();
        invalidateOptionsMenu();
        pager.setAdapter(adapter);
    }

    /**
     * Creates the pager and passes it to the {@link DotPageIndicator}
     */
    protected void configureDotPager() {
        if (adapter == null) {
            createPager();
            dotPageIndicator.setViewPager(pager);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentView());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            // On Lollipop, the action bar shadow is provided by default, so have to remove it explicitly
            getSupportActionBar().setElevation(0);
        }

        pager = (ViewPager) findViewById(R.id.vp_pages);
        pager.addOnPageChangeListener(this);
        dotPageIndicator = (DotPageIndicator) findViewById(R.id.dot_page_indicator);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pager.removeOnPageChangeListener(this);
    }

    @Override
    protected FragmentProvider getProvider() {
        return null;
    }

    public int getContentView() {
        return R.layout.pager_with_dots;
    }

    public ViewPager getViewPager(){
        return pager;
    }
}
