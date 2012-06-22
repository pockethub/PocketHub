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
import static com.github.mobile.Intents.EXTRA_ISSUE;
import static com.github.mobile.Intents.EXTRA_ISSUE_FILTER;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import static com.github.mobile.RequestCodes.ISSUE_CREATE;
import static com.github.mobile.RequestCodes.ISSUE_FILTER_EDIT;
import static com.github.mobile.RequestCodes.ISSUE_VIEW;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
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

    private View filterHeader;

    private TextView state;

    private ImageView assigneeAvatar;

    private View assigneeArea;

    private TextView assignee;

    private TextView labels;

    private TextView milestone;

    @Inject
    private AvatarLoader avatars;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (filter == null)
            filter = new IssueFilter(repository);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        filterHeader = getLayoutInflater(savedInstanceState).inflate(
                layout.issue_filter_header, null);
        state = (TextView) filterHeader.findViewById(id.tv_filter_state);
        labels = (TextView) filterHeader.findViewById(id.tv_filter_labels);
        milestone = (TextView) filterHeader
                .findViewById(id.tv_filter_milestone);
        assigneeArea = filterHeader.findViewById(id.ll_assignee);
        assignee = (TextView) filterHeader.findViewById(id.tv_filter_assignee);
        assigneeAvatar = (ImageView) filterHeader
                .findViewById(id.iv_assignee_avatar);
        updateFilterSummary();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        getListAdapter().addHeader(filterHeader, null, false);
    }

    private void updateFilterSummary() {
        if (filter.isOpen())
            state.setText(string.open_issues);
        else
            state.setText(string.closed_issues);

        Collection<Label> filterLabels = filter.getLabels();
        if (filterLabels != null && !filterLabels.isEmpty()) {
            LabelDrawableSpan.setText(labels, filterLabels);
            labels.setVisibility(VISIBLE);
        } else
            labels.setVisibility(GONE);

        Milestone filterMilestone = filter.getMilestone();
        if (filterMilestone != null) {
            milestone.setText(filterMilestone.getTitle());
            milestone.setVisibility(VISIBLE);
        } else
            milestone.setVisibility(GONE);

        User user = filter.getAssignee();
        if (user != null) {
            avatars.bind(assigneeAvatar, user);
            assignee.setText(user.getLogin());
            assigneeArea.setVisibility(VISIBLE);
        } else
            assigneeArea.setVisibility(GONE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_issues);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivityForResult(
                IssuesViewActivity.createIntent(items, repository, position
                        - getListAdapter().getHeadersCount()), ISSUE_VIEW);
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(menu.issues, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isUsable())
            return false;
        switch (item.getItemId()) {
        case id.create_issue:
            startActivityForResult(EditIssueActivity.createIntent(repository),
                    ISSUE_CREATE);
            return true;
        case id.m_filter:
            startActivityForResult(FilterIssuesActivity.createIntent(filter),
                    ISSUE_FILTER_EDIT);
            return true;
        case id.m_bookmark:
            cache.addIssueFilter(filter, new RequestFuture<IssueFilter>() {

                public void success(IssueFilter response) {
                    ToastUtils.show(getActivity(), string.message_filter_saved);
                }
            });
            return true;
        case id.m_search:
            Bundle args = new Bundle();
            args.putSerializable(EXTRA_REPOSITORY, repository);
            getActivity().startSearch(null, false, args, false);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == ISSUE_FILTER_EDIT
                && data != null) {
            IssueFilter newFilter = (IssueFilter) data
                    .getSerializableExtra(EXTRA_ISSUE_FILTER);
            if (!filter.equals(newFilter)) {
                filter = newFilter;
                updateFilterSummary();
                pager.reset();
                refreshWithProgress();
                return;
            }
        }

        if (requestCode == ISSUE_VIEW) {
            getListAdapter().getWrappedAdapter().notifyDataSetChanged();
            forceRefresh();
            return;
        }

        if (requestCode == ISSUE_CREATE && resultCode == RESULT_OK) {
            Issue created = (Issue) data.getSerializableExtra(EXTRA_ISSUE);
            forceRefresh();
            startActivityForResult(
                    IssuesViewActivity.createIntent(created, repository),
                    ISSUE_VIEW);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected ResourcePager<Issue> createPager() {
        return new IssuePager(store) {

            public PageIterator<Issue> createIterator(int page, int size) {
                return service.pageIssues(repository, filter.toFilterMap(),
                        page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_issues;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return string.error_issues_load;
    }

    @Override
    protected ItemListAdapter<Issue, ? extends ItemView> createAdapter(
            List<Issue> items) {
        return new RepositoryIssueListAdapter(
                getActivity().getLayoutInflater(),
                items.toArray(new Issue[items.size()]), avatars);
    }
}
