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

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.Intents.EXTRA_ISSUE_FILTER;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import static com.github.mobile.RequestCodes.ISSUE_CREATE;
import static com.github.mobile.RequestCodes.ISSUE_FILTER_EDIT;
import static com.github.mobile.RequestCodes.ISSUE_VIEW;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.R.menu;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.RequestFuture;
import com.github.mobile.core.ResourcePager;
import com.github.mobile.core.issue.IssueFilter;
import com.github.mobile.core.issue.IssuePager;
import com.github.mobile.core.issue.IssueStore;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;
import com.github.mobile.ui.PagedItemFragment;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.ListViewUtils;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;

import roboguice.inject.InjectExtra;

/**
 * Fragment to display a list of issues
 */
public class IssuesFragment extends PagedItemFragment<Issue> {

    @Inject
    private AccountDataManager cache;

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    @InjectExtra(value = EXTRA_ISSUE_FILTER, optional = true)
    private IssueFilter filter;

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    private TextView filterTextView;

    @Inject
    private AvatarLoader avatarHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (filter == null)
            filter = new IssueFilter(repository);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View filterHeader = getLayoutInflater(savedInstanceState).inflate(layout.issue_filter_header, null);
        filterTextView = (TextView) filterHeader.findViewById(id.tv_filter_summary);
        getListView().addHeaderView(filterHeader, null, false);
        updateFilterSummary();
    }

    private void updateFilterSummary() {
        CharSequence display = filter.toDisplay();
        if (display.length() > 0) {
            filterTextView.setText(display);
            filterTextView.setVisibility(VISIBLE);
        } else
            filterTextView.setVisibility(GONE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(string.no_issues));
        ListViewUtils.configure(getActivity(), getListView(), true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivityForResult(ViewIssuesActivity.createIntent(items, position - l.getHeaderViewsCount()), ISSUE_VIEW);
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(menu.issues, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.create_issue:
            startActivityForResult(CreateIssueActivity.createIntent(repository), ISSUE_CREATE);
            return true;
        case id.filter_issues:
            startActivityForResult(FilterIssuesActivity.createIntent(repository, filter), ISSUE_FILTER_EDIT);
            return true;
        case id.bookmark_filter:
            cache.addIssueFilter(filter, new RequestFuture<IssueFilter>() {

                public void success(IssueFilter response) {
                    ToastUtils.show(getActivity(), string.message_filter_saved);
                }
            });
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == ISSUE_FILTER_EDIT && data != null) {
            IssueFilter newFilter = ((IssueFilter) data.getSerializableExtra(EXTRA_ISSUE_FILTER)).clone();
            if (!filter.equals(newFilter)) {
                filter = newFilter;
                updateFilterSummary();
                pager.reset();
                refreshWithProgress();
            }
        }

        if (requestCode == ISSUE_VIEW) {
            ListAdapter adapter = getListAdapter();
            if (adapter instanceof BaseAdapter)
                ((BaseAdapter) adapter).notifyDataSetChanged();
        }

        if (requestCode == ISSUE_CREATE && resultCode == RESULT_OK)
            refresh();

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected ResourcePager<Issue> createPager() {
        return new IssuePager(store) {

            public PageIterator<Issue> createIterator(int page, int size) {
                return service.pageIssues(repository, filter.toFilterMap(), page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_issues;
    }

    protected ItemListAdapter<Issue, ? extends ItemView> createAdapter(List<Issue> items) {
        return new RepositoryIssueListAdapter(getActivity().getLayoutInflater(),
                items.toArray(new Issue[items.size()]), avatarHelper);
    }
}
