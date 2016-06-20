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

    private final Resources resources;

    private final boolean hasIssues;

    private final boolean hasReadme;

    private RepositoryCodeFragment codeFragment;

    private CommitListFragment commitsFragment;

    /**
     * Create repository pager adapter
     *
     * @param activity
     * @param hasIssues
     */
    public RepositoryPagerAdapter(AppCompatActivity activity,
                                  boolean hasIssues, boolean hasReadme) {
        super(activity);

        resources = activity.getResources();
        this.hasReadme = hasReadme;
        this.hasIssues = hasIssues;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        position = hasReadme ? position : position + 1;

        switch (position) {
            case 0:
                return resources.getString(R.string.tab_readme);
            case 1:
                return resources.getString(R.string.tab_news);
            case 2:
                return resources.getString(R.string.tab_code);
            case 3:
                return resources.getString(R.string.tab_commits);
            case 4:
                return resources.getString(R.string.tab_issues);
            default:
                return null;
        }
    }

    @Override
    public Fragment getItem(int position) {
        position = hasReadme ? position : position + 1;

        switch (position) {
            case 0:
                return new RepositoryReadmeFragment();
            case 1:
                return new RepositoryNewsFragment();
            case 2:
                codeFragment = new RepositoryCodeFragment();
                return codeFragment;
            case 3:
                commitsFragment = new CommitListFragment();
                return commitsFragment;
            case 4:
                return new IssuesFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        int count = hasIssues ? 5 : 4;
        count = hasReadme ? count : count - 1;

        return count;
    }

    /**
     * Returns index of code page
     */
    public int getItemCode() {
        return hasReadme ? 2 : 1;
    }

    /**
     * Returns index of commits page
     */
    public int getItemCommits() {
        return hasReadme ? 3 : 2;
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
        if (position == getItemCode() && codeFragment != null)
            codeFragment.onDialogResult(requestCode, resultCode, arguments);
        else if (position == getItemCommits() && commitsFragment != null)
            commitsFragment.onDialogResult(requestCode, resultCode, arguments);

        return this;
    }
}