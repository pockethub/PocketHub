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
package com.github.pockethub.ui.repo;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.github.pockethub.R;
import com.github.pockethub.ui.FragmentPagerAdapter;
import com.github.pockethub.ui.code.RepositoryCodeFragment;
import com.github.pockethub.ui.commit.CommitListFragment;
import com.github.pockethub.ui.issue.IssuesFragment;

/**
 * Adapter to view a repository's various pages
 */
public class RepositoryPagerAdapter extends FragmentPagerAdapter {

    /**
     * Index of code page
     */
    public static final int ITEM_CODE = 1;

    /**
     * Index of commits page
     */
    public static final int ITEM_COMMITS = 2;

    private final Resources resources;

    private final boolean hasIssues;

    private RepositoryCodeFragment codeFragment;

    private CommitListFragment commitsFragment;

    /**
     * Create repository pager adapter
     *
     * @param activity
     * @param hasIssues
     */
    public RepositoryPagerAdapter(AppCompatActivity activity,
            boolean hasIssues) {
        super(activity);

        resources = activity.getResources();
        this.hasIssues = hasIssues;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
        case 0:
            return resources.getString(R.string.tab_news);
        case 1:
            return resources.getString(R.string.tab_code);
        case 2:
            return resources.getString(R.string.tab_commits);
        case 3:
            return resources.getString(R.string.tab_issues);
        default:
            return null;
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
        case 0:
            return new RepositoryNewsFragment();
        case 1:
            codeFragment = new RepositoryCodeFragment();
            return codeFragment;
        case 2:
            commitsFragment = new CommitListFragment();
            return commitsFragment;
        case 3:
            return new IssuesFragment();
        default:
            return null;
        }
    }

    @Override
    public int getCount() {
        return hasIssues ? 4 : 3;
    }

    /**
     * Pass back button pressed event down to fragments
     *
     * @return true if handled, false otherwise
     */
    public boolean onBackPressed() {
        return codeFragment != null && codeFragment.onBackPressed();
    }

    /**
     * Deliver dialog result to fragment at given position
     *
     * @param position
     * @param requestCode
     * @param resultCode
     * @param arguments
     * @return this adapter
     */
    public RepositoryPagerAdapter onDialogResult(int position, int requestCode,
            int resultCode, Bundle arguments) {
        if (position == ITEM_CODE && codeFragment != null)
            codeFragment.onDialogResult(requestCode, resultCode, arguments);
        else if (position == ITEM_COMMITS && commitsFragment != null)
            commitsFragment.onDialogResult(requestCode, resultCode, arguments);

        return this;
    }
}
