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
package com.github.mobile.ui.user;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.github.mobile.repo.RepoListFragment;
import com.viewpagerindicator.TitleProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * Pager adapter for a user's different views
 */
public class UserPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

    private final boolean defaultUser;

    private final FragmentManager fragmentManager;

    private final Set<String> tags = new HashSet<String>();

    /**
     * @param fm
     * @param defaultUser
     */
    public UserPagerAdapter(final FragmentManager fm, final boolean defaultUser) {
        super(fm);

        fragmentManager = fm;
        this.defaultUser = defaultUser;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
        case 0:
            return new UserNewsFragment();
        case 1:
            return new RepoListFragment();
        case 2:
            return defaultUser ? new FollowersFragment() : new MembersFragment();
        default:
            return null;
        }
    }

    /**
     * This methods clears any fragments that may not apply to the newly selected org.
     *
     * @return this adapter
     */
    public UserPagerAdapter clearAdapter() {
        if (tags.isEmpty())
            return this;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (String tag : tags) {
            Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if (fragment != null)
                transaction.remove(fragment);
        }
        transaction.commit();

        return this;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        Object fragment = super.instantiateItem(container, position);
        if (fragment instanceof Fragment)
            tags.add(((Fragment) fragment).getTag());
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public String getTitle(int position) {
        switch (position) {
        case 0:
            return "News";
        case 1:
            return "Repos";
        case 2:
            return defaultUser ? "Followers" : "Members";
        default:
            return null;
        }
    }
}
