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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.core.ResourcePager;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.service.issues.IssueService;
import com.google.inject.Inject;

import java.util.List;
import java.util.Map;

import static com.github.pockethub.android.RequestCodes.ISSUE_VIEW;

/**
 * Fragment to display a pageable list of dashboard issues
 */
public class DashboardIssueFragment extends PagedItemFragment<Issue> {

    /**
     * Filter data argument
     */
    public static final String ARG_FILTER = "filter";

    @Inject
    private IssueStore store;

    @Inject
    private AvatarLoader avatars;

    private Map<String, Object> filterData;

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        filterData = (Map<String, Object>) getArguments().getSerializable(ARG_FILTER);
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
                return resource.id();
            }

            @Override
            public PageIterator<Issue> createIterator(int page, int size) {
                return new PageIterator<>(page1 ->
                        ServiceGenerator.createService(getActivity(), IssueService.class)
                                .getIssues(filterData, page1), page);
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
