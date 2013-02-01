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

import android.content.res.Resources;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.github.mobile.R.string;
import com.github.mobile.ui.FragmentPagerAdapter;

/**
 * Adapter to view various pages of search screen
 */
public class SearchPagerAdapter extends FragmentPagerAdapter {

    private final Resources resources;

    /**
     * Create search pager adapter
     *
     * @param activity
     */
    public SearchPagerAdapter(SherlockFragmentActivity activity) {
        super(activity);

        resources = activity.getResources();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return resources.getString(string.tab_repositories);
            case 1:
                return resources.getString(string.tab_users);
            default:
                return null;
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new SearchRepositoryListFragment();
            case 1:
                return new SearchUserListFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
