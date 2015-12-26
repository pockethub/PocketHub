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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.Set;

/**
 * Pager adapter that provides the current fragment
 */
public abstract class FragmentPagerAdapter extends
    android.support.v4.app.FragmentPagerAdapter implements FragmentProvider {

    private final AppCompatActivity activity;

    private final FragmentManager fragmentManager;

    private Fragment selected;

    private final Set<String> tags = new HashSet<>();

    private int containerId;

    /**
     * @param activity
     */
    public FragmentPagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());

        fragmentManager = activity.getSupportFragmentManager();
        this.activity = activity;
    }

    public FragmentPagerAdapter(Fragment fragment) {
        super(fragment.getChildFragmentManager());

        fragmentManager = fragment.getChildFragmentManager();
        this.activity = (AppCompatActivity) fragment.getActivity();
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }

    /**
     * This methods clears any fragments that may not apply to the newly
     * selected org.
     *
     * @return this adapter
     */
    public FragmentPagerAdapter clearAdapter() {
        if (tags.isEmpty())
            return this;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (String tag : tags) {
            Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if (fragment != null)
                transaction.remove(fragment);
        }
        transaction.commit();
        tags.clear();

        return this;
    }

    @Override
    public Fragment getSelected() {
        return selected;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        Object fragment = super.instantiateItem(container, position);
        containerId = container.getId();
        if (fragment instanceof Fragment) {
            tags.add(((Fragment) fragment).getTag());
        }

        return fragment;
    }

    /**
     * This method is used to get a reference to created fragments in the adapter.
     *
     * @param fragmentPosition
     * position of the fragment in the pager.
     *
     * @return corresponding fragment that is created
     * during {@link #instantiateItem(ViewGroup, int)}
     *
     */
    public Fragment getFragmentByPosition(int fragmentPosition) {
        String fragmentTag = getFragmentTag(containerId, fragmentPosition);
        return fragmentManager.findFragmentByTag(fragmentTag);
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position,
        final Object object) {
        super.setPrimaryItem(container, position, object);

        boolean changed = false;
        if (object instanceof Fragment) {
            changed = object != selected;
            selected = (Fragment) object;
        } else {
            changed = object != null;
            selected = null;
        }

        if (changed)
            activity.invalidateOptionsMenu();
    }

    private String getFragmentTag(int viewPagerId, int fragmentPosition) {
        return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
    }
}
