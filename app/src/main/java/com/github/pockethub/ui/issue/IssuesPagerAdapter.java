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
package com.github.pockethub.ui.issue;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.core.issue.IssueStore;
import com.github.pockethub.ui.FragmentStatePagerAdapter;

import java.util.List;

import static com.github.pockethub.Intents.EXTRA_ISSUE_NUMBER;
import static com.github.pockethub.Intents.EXTRA_IS_COLLABORATOR;
import static com.github.pockethub.Intents.EXTRA_IS_OWNER;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY_NAME;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY_OWNER;
import static com.github.pockethub.Intents.EXTRA_USER;


/**
 * Adapter to page through an {@link Issue} array
 */
public class IssuesPagerAdapter extends FragmentStatePagerAdapter {

    private final Repo repo;

    private final List<Repo> repos;

    private final int[] issues;

    private final SparseArray<IssueFragment> fragments = new SparseArray<>();

    private final IssueStore store;

    private boolean isCollaborator;

    private boolean isOwner;

    /**
     * @param activity
     * @param repoIds
     * @param issueNumbers
     * @param issueStore
     * @param collaborator
     */
    public IssuesPagerAdapter(AppCompatActivity activity,
            List<Repo> repoIds, int[] issueNumbers,
            IssueStore issueStore, boolean collaborator, boolean owner) {
        super(activity);

        repos = repoIds;
        repo = null;
        issues = issueNumbers;
        store = issueStore;
        isCollaborator = collaborator;
        isOwner = owner;
    }

    /**
     * @param activity
     * @param repository
     * @param issueNumbers
     * @param collaborator
     */
    public IssuesPagerAdapter(AppCompatActivity activity,
            Repo repository, int[] issueNumbers,
            boolean collaborator, boolean owner) {
        super(activity);

        repos = null;
        repo = repository;
        issues = issueNumbers;
        store = null;
        isCollaborator = collaborator;
        isOwner = owner;
    }

    @Override
    public Fragment getItem(int position) {
        IssueFragment fragment = new IssueFragment();
        Bundle args = new Bundle();
        if (repo != null) {
            args.putString(EXTRA_REPOSITORY_NAME, repo.name);
            User owner = repo.owner;
            args.putString(EXTRA_REPOSITORY_OWNER, owner.login);
            args.putParcelable(EXTRA_USER, owner);
        } else {
            Repo repo = repos.get(position);
            args.putString(EXTRA_REPOSITORY_NAME, repo.name);
            args.putString(EXTRA_REPOSITORY_OWNER, repo.owner.login);
            Issue issue = store.getIssue(repo, issues[position]);
            if (issue != null && issue.user != null) {
                Repo fullRepo = issue.repository;
                if (fullRepo != null && fullRepo.owner != null)
                    args.putParcelable(EXTRA_USER, fullRepo.owner);
            }
        }
        args.putInt(EXTRA_ISSUE_NUMBER, issues[position]);
        args.putBoolean(EXTRA_IS_COLLABORATOR, isCollaborator);
        args.putBoolean(EXTRA_IS_OWNER, isOwner);
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
    public IssuesPagerAdapter onDialogResult(int position, int requestCode,
            int resultCode, Bundle arguments) {
        IssueFragment fragment = fragments.get(position);
        if (fragment != null)
            fragment.onDialogResult(requestCode, resultCode, arguments);
        return this;
    }
}
