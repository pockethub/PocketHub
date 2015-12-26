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
package com.github.pockethub.ui.user;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.github.pockethub.R;
import com.github.pockethub.ui.FragmentPagerAdapter;
import com.github.pockethub.ui.repo.UserRepositoryListFragment;

/**
 * Pager adapter for a user's different views
 */
public class UserPagerAdapter extends FragmentPagerAdapter {

    private final Resources resources;

    /**
     * @param activity
     */
    public UserPagerAdapter(final AppCompatActivity activity) {
        super(activity);

        resources = activity.getResources();
    }

    @Override
    public Fragment getItem(final int position) {
        switch (position) {
        case 0:
            return new UserCreatedNewsFragment();
        case 1:
            return new UserRepositoryListFragment();
        case 2:
            return new UserFollowersFragment();
        case 3:
            return new UserFollowingFragment();
        default:
            return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
        case 0:
            return resources.getString(R.string.tab_news);
        case 1:
            return resources.getString(R.string.tab_repositories);
        case 2:
            return resources.getString(R.string.tab_followers);
        case 3:
            return resources.getString(R.string.tab_following);
        default:
            return null;
        }
    }
}
