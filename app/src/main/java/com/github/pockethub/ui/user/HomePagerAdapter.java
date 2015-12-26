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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.R;
import com.github.pockethub.ui.FragmentPagerAdapter;
import com.github.pockethub.ui.repo.RepositoryListFragment;

import java.util.HashSet;
import java.util.Set;

/**
 * Pager adapter for a user's different views
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

    private final User org;

    private boolean defaultUser;

    private final FragmentManager fragmentManager;

    private final Resources resources;

    private final Set<String> tags = new HashSet<>();

    /**
     * @param fragment
     * @param defaultUser
     */
    public HomePagerAdapter(final Fragment fragment,
        final boolean defaultUser, final User org) {
        super(fragment);

        this.org = org;
        fragmentManager = fragment.getChildFragmentManager();
        resources = fragment.getResources();
        this.defaultUser = defaultUser;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = defaultUser ? new UserReceivedNewsFragment()
                    : new OrganizationNewsFragment();
                break;
            case 1:
                fragment = new RepositoryListFragment();
                break;
            case 2:
                fragment = defaultUser ? new MyFollowersFragment()
                    : new MembersFragment();
                break;
            case 3:
                fragment = new MyFollowingFragment();
                break;
        }

        if (fragment != null) {
            Bundle args = new Bundle();
            args.putParcelable("org", org);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        Object fragment = super.instantiateItem(container, position);
        if (fragment instanceof Fragment)
            tags.add(((Fragment) fragment).getTag());
        return fragment;
    }

    @Override
    public int getCount() {
        return defaultUser ? 4 : 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.tab_news);
            case 1:
                return resources.getString(R.string.tab_repositories);
            case 2:
                return resources.getString(defaultUser ? R.string.tab_followers_self
                    : R.string.tab_members);
            case 3:
                return resources.getString(R.string.tab_following_self);
            default:
                return null;
        }
    }
}
