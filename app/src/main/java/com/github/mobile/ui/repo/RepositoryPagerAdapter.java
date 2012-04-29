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
package com.github.mobile.ui.repo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.mobile.issue.IssuesFragment;
import com.viewpagerindicator.TitleProvider;

/**
 * Adapter to view a repository's various pages
 */
public class RepositoryPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

    /**
     * @param fm
     */
    public RepositoryPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public String getTitle(int position) {
        switch (position) {
        case 0:
            return "News";
        case 1:
            return "Issues";
        default:
            return null;
        }
    }

    public Fragment getItem(int position) {
        switch (position) {
        case 0:
            return new RepositoryNewsFragment();
        case 1:
            return new IssuesFragment();
        default:
            return null;
        }
    }

    public int getCount() {
        return 2;
    }
}
