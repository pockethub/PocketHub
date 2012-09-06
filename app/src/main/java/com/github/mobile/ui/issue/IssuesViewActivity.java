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

import static com.github.mobile.Intents.EXTRA_ISSUE_NUMBERS;
import static com.github.mobile.Intents.EXTRA_POSITION;
import static com.github.mobile.Intents.EXTRA_REPOSITORIES;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.core.issue.IssueStore;
import com.github.mobile.core.issue.IssueUtils;
import com.github.mobile.core.repo.RefreshRepositoryTask;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.UrlLauncher;
import com.github.mobile.ui.ViewPager;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity to display a collection of issues or pull requests in a pager
 */
public class IssuesViewActivity extends DialogFragmentActivity implements
        OnPageChangeListener {

    private static final String EXTRA_PULL_REQUESTS = "pullRequests";

    /**
     * Create an intent to show a single issue
     *
     * @param issue
     * @return intent
     */
    public static Intent createIntent(final Issue issue) {
        return createIntent(Collections.singletonList(issue), 0);
    }

    /**
     * Create an intent to show issue
     *
     * @param issue
     * @param repository
     * @return intent
     */
    public static Intent createIntent(final Issue issue,
            final Repository repository) {
        return createIntent(Collections.singletonList(issue), repository, 0);
    }

    /**
     * Create an intent to show issues with an initial selected issue
     *
     * @param issues
     * @param repository
     * @param position
     * @return intent
     */
    public static Intent createIntent(final Collection<? extends Issue> issues,
            final Repository repository, final int position) {
        int[] numbers = new int[issues.size()];
        boolean[] pullRequests = new boolean[issues.size()];
        int index = 0;
        for (Issue issue : issues) {
            numbers[index] = issue.getNumber();
            pullRequests[index] = IssueUtils.isPullRequest(issue);
            index++;
        }
        return new Builder("issues.VIEW").add(EXTRA_ISSUE_NUMBERS, numbers)
                .add(EXTRA_REPOSITORY, repository)
                .add(EXTRA_POSITION, position)
                .add(EXTRA_PULL_REQUESTS, pullRequests).toIntent();
    }

    /**
     * Create an intent to show issues with an initial selected issue
     *
     * @param issues
     * @param position
     * @return intent
     */
    public static Intent createIntent(Collection<? extends Issue> issues,
            int position) {
        final int count = issues.size();
        int[] numbers = new int[count];
        boolean[] pullRequests = new boolean[count];
        ArrayList<RepositoryId> repos = new ArrayList<RepositoryId>(count);
        int index = 0;
        for (Issue issue : issues) {
            numbers[index] = issue.getNumber();
            pullRequests[index] = IssueUtils.isPullRequest(issue);
            index++;

            RepositoryId repoId = null;
            if (issue instanceof RepositoryIssue) {
                Repository issueRepo = ((RepositoryIssue) issue)
                        .getRepository();
                if (issueRepo != null) {
                    User owner = issueRepo.getOwner();
                    if (owner != null)
                        repoId = RepositoryId.create(owner.getLogin(),
                                issueRepo.getName());
                }
            }
            if (repoId == null)
                repoId = RepositoryId.createFromUrl(issue.getHtmlUrl());
            repos.add(repoId);
        }

        Builder builder = new Builder("issues.VIEW");
        builder.add(EXTRA_ISSUE_NUMBERS, numbers);
        builder.add(EXTRA_REPOSITORIES, repos);
        builder.add(EXTRA_POSITION, position);
        builder.add(EXTRA_PULL_REQUESTS, pullRequests);
        return builder.toIntent();
    }

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @InjectExtra(EXTRA_ISSUE_NUMBERS)
    private int[] issueNumbers;

    @InjectExtra(EXTRA_PULL_REQUESTS)
    private boolean[] pullRequests;

    @InjectExtra(value = EXTRA_REPOSITORIES, optional = true)
    private ArrayList<RepositoryId> repoIds;

    @InjectExtra(value = EXTRA_REPOSITORY, optional = true)
    private Repository repo;

    @InjectExtra(EXTRA_POSITION)
    private int initialPosition;

    @Inject
    private AvatarLoader avatars;

    @Inject
    private IssueStore store;

    private AtomicReference<User> user = new AtomicReference<User>();

    private IssuesPagerAdapter adapter;

    private final UrlLauncher urlLauncher = new UrlLauncher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager);

        if (repo != null)
            adapter = new IssuesPagerAdapter(this, repo, issueNumbers);
        else
            adapter = new IssuesPagerAdapter(this, repoIds, issueNumbers, store);
        pager.setAdapter(adapter);

        pager.setOnPageChangeListener(this);
        pager.scheduleSetItem(initialPosition, this);
        onPageSelected(initialPosition);

        if (repo != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setSubtitle(repo.generateId());
            user.set(repo.getOwner());
            avatars.bind(actionBar, user);
        }

        // Load avatar if single issue and user is currently unset or missing
        // avatar URL
        if (issueNumbers.length == 1
                && (user.get() == null || user.get().getAvatarUrl() == null))
            new RefreshRepositoryTask(this, repo != null ? repo
                    : repoIds.get(0)) {

                @Override
                protected void onSuccess(Repository fullRepository)
                        throws Exception {
                    super.onSuccess(fullRepository);

                    avatars.bind(getSupportActionBar(),
                            fullRepository.getOwner());
                }
            }.execute();
    }

    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
        // Intentionally left blank
    }

    private void updateTitle(final int position) {
        int number = issueNumbers[position];
        boolean pullRequest = pullRequests[position];

        if (pullRequest)
            getSupportActionBar().setTitle(
                    getString(string.pull_request_title) + number);
        else
            getSupportActionBar().setTitle(
                    getString(string.issue_title) + number);
    }

    public void onPageSelected(final int position) {
        if (repo != null) {
            updateTitle(position);
            return;
        }

        if (repoIds == null)
            return;

        ActionBar actionBar = getSupportActionBar();
        RepositoryId repoId = repoIds.get(position);
        if (repoId != null) {
            updateTitle(position);
            actionBar.setSubtitle(repoId.generateId());
            RepositoryIssue issue = store.getIssue(repoId,
                    issueNumbers[position]);
            if (issue != null) {
                Repository fullRepo = issue.getRepository();
                if (fullRepo != null && fullRepo.getOwner() != null) {
                    user.set(fullRepo.getOwner());
                    avatars.bind(actionBar, user);
                } else
                    actionBar.setLogo(null);
            } else
                actionBar.setLogo(null);
        } else {
            actionBar.setSubtitle(null);
            actionBar.setLogo(null);
        }
    }

    public void onPageScrollStateChanged(int state) {
        // Intentionally left blank
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        adapter.onDialogResult(pager.getCurrentItem(), requestCode, resultCode,
                arguments);
    }

    @Override
    public void startActivity(Intent intent) {
        Intent converted = urlLauncher.convert(intent);
        if (converted != null)
            super.startActivity(converted);
        else
            super.startActivity(intent);
    }
}
