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
import android.R.integer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.UrlLauncher;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity display a collection of issues in a pager
 */
public class ViewIssuesActivity extends DialogFragmentActivity implements OnPageChangeListener {

    /**
     * Create an intent to show a single issue
     *
     * @param issue
     * @return intent
     */
    public static Intent createIntent(Issue issue) {
        List<Issue> list = new ArrayList<Issue>(1);
        list.add(issue);
        return createIntent(list, 0);
    }

    /**
     * Create an intent to show issues with an initial selected issue
     *
     * @param issues
     * @param repository
     * @param position
     * @return intent
     */
    public static Intent createIntent(Collection<? extends Issue> issues, Repository repository, int position) {
        ArrayList<Integer> numbers = new ArrayList<Integer>(issues.size());
        for (Issue issue : issues)
            numbers.add(issue.getNumber());
        return new Builder("issues.VIEW").add(EXTRA_ISSUE_NUMBERS, numbers).add(EXTRA_REPOSITORY, repository)
                .add(EXTRA_POSITION, position).toIntent();
    }

    /**
     * Create an intent to show issues with an initial selected issue
     *
     * @param issues
     * @param position
     * @return intent
     */
    public static Intent createIntent(Collection<? extends Issue> issues, int position) {
        ArrayList<Integer> numbers = new ArrayList<Integer>(issues.size());
        ArrayList<RepositoryId> repos = new ArrayList<RepositoryId>(issues.size());
        for (Issue issue : issues) {
            numbers.add(issue.getNumber());
            repos.add(RepositoryId.createFromUrl(issue.getHtmlUrl()));
        }
        return new Builder("issues.VIEW").add(EXTRA_ISSUE_NUMBERS, numbers).add(EXTRA_REPOSITORIES, repos)
                .add(EXTRA_POSITION, position).toIntent();
    }

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @InjectExtra(EXTRA_ISSUE_NUMBERS)
    private ArrayList<integer> issueIds;

    @InjectExtra(value = EXTRA_REPOSITORIES, optional = true)
    private ArrayList<RepositoryId> repoIds;

    @InjectExtra(value = EXTRA_REPOSITORY, optional = true)
    private Repository repo;

    @InjectExtra(EXTRA_POSITION)
    private int initialPosition;

    @Inject
    private AvatarLoader avatars;

    private IssuesPagerAdapter adapter;

    private final UrlLauncher urlLauncher = new UrlLauncher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager);

        Integer[] numbers = issueIds.toArray(new Integer[issueIds.size()]);
        if (repo != null)
            adapter = new IssuesPagerAdapter(getSupportFragmentManager(), repo, numbers);
        else
            adapter = new IssuesPagerAdapter(getSupportFragmentManager(), repoIds.toArray(new RepositoryId[repoIds
                    .size()]), numbers);
        pager.setAdapter(adapter);

        pager.setOnPageChangeListener(this);
        pager.setCurrentItem(initialPosition);
        onPageSelected(initialPosition);

        if (repo != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setSubtitle(repo.generateId());
            avatars.bind(actionBar, repo.getOwner());
        }
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Intentionally left blank
    }

    public void onPageSelected(int position) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(string.issue_title) + issueIds.get(position));
        if (repo == null && repoIds != null)
            actionBar.setSubtitle(repoIds.get(position).generateId());
    }

    public void onPageScrollStateChanged(int state) {
        // Intentionally left blank
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        adapter.onDialogResult(pager.getCurrentItem(), requestCode, resultCode, arguments);
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
