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
package com.github.mobile.ui.issue;

import static com.github.mobile.util.GitHubIntents.EXTRA_ISSUE_NUMBER;
import static com.github.mobile.util.GitHubIntents.EXTRA_REPOSITORY_NAME;
import static com.github.mobile.util.GitHubIntents.EXTRA_REPOSITORY_OWNER;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;

/**
 * Adapter to page through an {@link Issue} array
 */
public class IssuesPagerAdapter extends FragmentPagerAdapter {

    private final RepositoryId[] repos;

    private final Integer[] issues;

    private final SparseArray<IssueFragment> fragments = new SparseArray<IssueFragment>();

    /**
     * @param fm
     * @param repoIds
     * @param issueNumbers
     */
    public IssuesPagerAdapter(FragmentManager fm, RepositoryId[] repoIds, Integer[] issueNumbers) {
        super(fm);

        repos = repoIds;
        issues = issueNumbers;
    }

    @Override
    public Fragment getItem(int position) {
        IssueFragment fragment = new IssueFragment();
        Bundle args = new Bundle();
        RepositoryId repo = repos[position];
        args.putString(EXTRA_REPOSITORY_NAME, repo.getName());
        args.putString(EXTRA_REPOSITORY_OWNER, repo.getOwner());
        args.putInt(EXTRA_ISSUE_NUMBER, issues[position]);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

        fragments.remove(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object fragment = super.instantiateItem(container, position);
        if (fragment instanceof IssueFragment)
            fragments.put(position, (IssueFragment) fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return issues.length;
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
    public IssuesPagerAdapter onDialogResult(int position, int requestCode, int resultCode, Bundle arguments) {
        IssueFragment fragment = fragments.get(position);
        if (fragment != null)
            fragment.onDialogResult(requestCode, resultCode, arguments);
        return this;
    }
}
