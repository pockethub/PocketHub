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
package com.github.pockethub.android.ui.issue;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.ui.FragmentStatePagerAdapter;

import java.util.List;

import static com.github.pockethub.android.Intents.*;


/**
 * Adapter to page through an {@link Issue} array
 */
public class IssuesPagerAdapter extends FragmentStatePagerAdapter {

    private final Repository repo;

    private final List<Repository> repos;

    private final int[] issues;

    private final SparseArray<IssueFragment> fragments = new SparseArray<>();

    private final IssueStore store;

    private boolean canWrite;

    /**
     * @param activity
     * @param repoIds
     * @param issueNumbers
     * @param issueStore
     * @param canWrite
     */
    public IssuesPagerAdapter(AppCompatActivity activity,
            List<Repository> repoIds, int[] issueNumbers,
            IssueStore issueStore, boolean canWrite) {
        super(activity);

        repos = repoIds;
        repo = null;
        issues = issueNumbers;
        store = issueStore;
        this.canWrite = canWrite;
    }

    /**
     * @param activity
     * @param repository
     * @param issueNumbers
     * @param canWrite
     */
    public IssuesPagerAdapter(AppCompatActivity activity,
            Repository repository, int[] issueNumbers,
            boolean canWrite) {
        super(activity);

        repos = null;
        repo = repository;
        issues = issueNumbers;
        store = null;
        this.canWrite = canWrite;
    }

    @Override
    public Fragment getItem(int position) {
        IssueFragment fragment = new IssueFragment();
        Bundle args = new Bundle();
        if (repo != null) {
            args.putString(EXTRA_REPOSITORY_NAME, repo.name());
            User owner = repo.owner();
            args.putString(EXTRA_REPOSITORY_OWNER, owner.login());
            args.putParcelable(EXTRA_USER, owner);
        } else {
            Repository repo = repos.get(position);
            args.putString(EXTRA_REPOSITORY_NAME, repo.name());
            args.putString(EXTRA_REPOSITORY_OWNER, repo.owner().login());
            Issue issue = store.getIssue(repo, issues[position]);
            if (issue != null && issue.user() != null) {
                Repository fullRepo = issue.repository();
                if (fullRepo != null && fullRepo.owner() != null) {
                    args.putParcelable(EXTRA_USER, fullRepo.owner());
                }
            }
        }
        args.putInt(EXTRA_ISSUE_NUMBER, issues[position]);
        args.putBoolean(EXTRA_CAN_WRITE_REPO, canWrite);
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
        if (fragment instanceof IssueFragment) {
            fragments.put(position, (IssueFragment) fragment);
        }
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
    public IssuesPagerAdapter onDialogResult(int position, int requestCode,
            int resultCode, Bundle arguments) {
        IssueFragment fragment = fragments.get(position);
        if (fragment != null) {
            fragment.onDialogResult(requestCode, resultCode, arguments);
        }
        return this;
    }
}
