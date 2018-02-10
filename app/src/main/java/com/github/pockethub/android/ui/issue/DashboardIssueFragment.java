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
import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.ui.item.issue.IssueDashboardItem;
import com.github.pockethub.android.ui.item.issue.IssueItem;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.service.issues.IssueService;
import com.xwray.groupie.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Single;
import retrofit2.Response;

import static com.github.pockethub.android.RequestCodes.ISSUE_VIEW;

/**
 * Fragment to display a pageable list of dashboard issues
 */
public class DashboardIssueFragment extends PagedItemFragment<Issue> {

    /**
     * Filter data argument
     */
    public static final String ARG_FILTER = "filter";

    private IssueService service = ServiceGenerator.createService(getActivity(), IssueService.class);

    @Inject
    protected IssueStore store;

    @Inject
    protected AvatarLoader avatars;

    private Map<String, Object> filterData;

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        filterData = (Map<String, Object>) getArguments().getSerializable(ARG_FILTER);
        super.onActivityCreated(savedInstanceState);
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
    protected Single<Response<Page<Issue>>> loadData(int page) {
        return service.getIssues(filterData, page);
    }

    @Override
    public void onItemClick(@NonNull Item clickedItem, @NonNull View view) {
        if (clickedItem instanceof IssueDashboardItem) {
            int position = getListAdapter().getAdapterPosition(clickedItem);
            Collection<Issue> issues = new ArrayList<>();
            for (Item item : items) {
                if (item instanceof IssueDashboardItem) {
                    issues.add(((IssueItem) item).getData());
                }
            }
            startActivityForResult(IssuesViewActivity.createIntent(issues, position), ISSUE_VIEW);
        }
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_issues;
    }

    @Override
    protected int getErrorMessage() {
        return R.string.error_issues_load;
    }

    @Override
    protected Item createItem(Issue item) {
        return new IssueDashboardItem(avatars, item);
    }
}
