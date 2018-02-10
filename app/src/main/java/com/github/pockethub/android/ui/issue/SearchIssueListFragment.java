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
import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.ui.item.issue.IssueItem;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.SearchPage;
import com.meisolsson.githubsdk.service.search.SearchService;
import com.xwray.groupie.Item;

import javax.inject.Inject;

import io.reactivex.Single;
import retrofit2.Response;

import static android.app.SearchManager.APP_DATA;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;

/**
 * Fragment to display a list of {@link Issue} instances
 */
public class SearchIssueListFragment extends PagedItemFragment<Issue> {

    SearchService service = ServiceGenerator.createService(getActivity(), SearchService.class);

    @Inject
    protected AvatarLoader avatars;

    private Repository repository;

    private String query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle appData = getActivity().getIntent().getBundleExtra(APP_DATA);
        if (appData != null) {
            repository = appData.getParcelable(EXTRA_REPOSITORY);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_issues);
    }

    /**
     * @param query
     * @return this fragment
     */
    public SearchIssueListFragment setQuery(final String query) {
        this.query = query;
        return this;
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof IssueItem) {
            Issue searchIssue = ((IssueItem) item).getData();
            startActivity(IssuesViewActivity.createIntent(searchIssue, repository));
        }
    }

    @Override
    protected Single<Response<Page<Issue>>> loadData(int page) {
        String searchQuery = query + "+repo:" + InfoUtils.createRepoId(repository);
        return service.searchIssues(searchQuery, null, null, page)
                .map(response -> {
                    SearchPage<Issue> issueSearchPage = response.body();

                    return Response.success(Page.<Issue>builder()
                            .first(issueSearchPage.first())
                            .last(issueSearchPage.last())
                            .next(issueSearchPage.next())
                            .prev(issueSearchPage.prev())
                            .items(issueSearchPage.items())
                            .build());
                });
    }

    @Override
    protected int getErrorMessage() {
        return R.string.error_issues_load;
    }


    @Override
    protected int getLoadingMessage() {
        return R.string.loading_issues;
    }

    @Override
    protected Item createItem(Issue item) {
        return new IssueItem(avatars, item, false);
    }
}
