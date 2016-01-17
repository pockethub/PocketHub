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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.services.client.GithubListClient;
import com.alorma.github.sdk.services.issues.GetIssuesClient;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.core.PageIterator;
import com.github.pockethub.core.ResourcePager;
import com.github.pockethub.core.issue.IssueStore;
import com.github.pockethub.ui.PagedItemFragment;
import com.github.pockethub.util.AvatarLoader;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.service.IssueService;

import java.util.List;
import java.util.Map;

import static com.github.pockethub.RequestCodes.ISSUE_VIEW;

/**
 * Fragment to display a pageable list of dashboard issues
 */
public class DashboardIssueFragment extends PagedItemFragment<Issue> {

    /**
     * Filter data argument
     */
    public static final String ARG_FILTER = "filter";

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    @Inject
    private AvatarLoader avatars;

    private Map<String, String> filterData;

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        filterData = (Map<String, String>) getArguments().getSerializable(ARG_FILTER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ISSUE_VIEW) {
            notifyDataSetChanged();
            forceRefresh();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivityForResult(
                IssuesViewActivity.createIntent(items, position
                        - getListAdapter().getHeadersCount()), ISSUE_VIEW);
    }

    @Override
    protected ResourcePager<Issue> createPager() {
        return new ResourcePager<Issue>() {

            @Override
            protected Issue register(Issue resource) {
                return store.addIssue(resource);
            }

            @Override
            protected Object getId(Issue resource) {
                return resource.id;
            }

            @Override
            public PageIterator<Issue> createIterator(int page, int size) {
                return new PageIterator<>(new PageIterator.GitHubRequest<List<Issue>>() {
                    @Override
                    public GithubListClient<List<Issue>> execute(int page) {
                        return new GetIssuesClient(filterData, page);
                    }
                }, page);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_issues;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_issues_load;
    }

    @Override
    protected SingleTypeAdapter<Issue> createAdapter(
            List<Issue> items) {
        return new DashboardIssueListAdapter(avatars, getActivity()
                .getLayoutInflater(), items.toArray(new Issue[items.size()]));
    }
}
