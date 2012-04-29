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

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.ResourcePager;
import com.github.mobile.R.string;
import com.github.mobile.issue.IssueStore;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;
import com.github.mobile.ui.PagedItemFragment;
import com.github.mobile.util.AvatarUtils;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;

import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Fragment to display a pageable list of dashboard issues
 */
public class DashboardIssueFragment extends PagedItemFragment<RepositoryIssue> {

    /**
     * Filter data argument
     */
    public static final String ARG_FILTER = "filter";

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    @Inject
    private AvatarUtils avatarHelper;

    private Map<String, String> filterData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        filterData = (Map<String, String>) getArguments().getSerializable(ARG_FILTER);
        ListViewUtils.configure(getActivity(), getListView(), true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivity(ViewIssuesActivity.createIntent(items, position - l.getHeaderViewsCount()));
    }

    @Override
    protected ResourcePager<RepositoryIssue> createPager() {
        return new ResourcePager<RepositoryIssue>() {

            @Override
            protected RepositoryIssue register(RepositoryIssue resource) {
                return store.addIssue(resource);
            }

            @Override
            protected Object getId(RepositoryIssue resource) {
                return resource.getId();
            }

            @Override
            public PageIterator<RepositoryIssue> createIterator(int page, int size) {
                return service.pageIssues(filterData, page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_issues;
    }

    @Override
    protected ItemListAdapter<RepositoryIssue, ? extends ItemView> createAdapter(List<RepositoryIssue> items) {
        return new DashboardIssueListAdapter(avatarHelper, getActivity().getLayoutInflater(),
                items.toArray(new RepositoryIssue[items.size()]));
    }
}
