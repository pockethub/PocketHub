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

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.github.pockethub.android.ui.item.issue.IssueFilterHeaderItem;
import com.github.pockethub.android.ui.item.issue.IssueItem;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueFilter;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.persistence.AccountDataManager;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.service.issues.IssueService;
import com.xwray.groupie.Item;

import javax.inject.Inject;

import java.util.Collection;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.github.pockethub.android.Intents.EXTRA_ISSUE;
import static com.github.pockethub.android.Intents.EXTRA_ISSUE_FILTER;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;
import static com.github.pockethub.android.RequestCodes.ISSUE_CREATE;
import static com.github.pockethub.android.RequestCodes.ISSUE_FILTER_EDIT;
import static com.github.pockethub.android.RequestCodes.ISSUE_VIEW;

/**
 * Fragment to display a list of issues
 */
public class IssuesFragment extends PagedItemFragment<Issue> {

    IssueService service = ServiceGenerator.createService(getActivity(), IssueService.class);

    @Inject
    protected AccountDataManager cache;

    @Inject
    protected IssueStore store;

    private IssueFilter filter;

    private Repository repository;

    @Inject
    protected AvatarLoader avatars;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        filter = getParcelableExtra(EXTRA_ISSUE_FILTER);
        repository = getParcelableExtra(EXTRA_REPOSITORY);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (filter == null) {
            filter = new IssueFilter(repository);
        }
    }

    @Override
    protected void configureList(RecyclerView recyclerView) {
        super.configureList(recyclerView);
        getMainSection().setHeader(new IssueFilterHeaderItem(avatars, filter));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_issues);
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof IssueItem) {
            // Remove one since we have a header
            int position = getListAdapter().getAdapterPosition(item) - 1;
            Collection<Issue> issues = Observable.fromIterable(items)
                    .filter(mapItem -> mapItem instanceof IssueItem)
                    .map(mapItem -> ((IssueItem) mapItem).getData())
                    .toList()
                    .blockingGet();

            startActivityForResult(
                    IssuesViewActivity.createIntent(issues, repository, position), ISSUE_VIEW);
        } else if (item instanceof IssueFilterHeaderItem) {
            startActivityForResult(
                    EditIssuesFilterActivity.createIntent(filter), ISSUE_FILTER_EDIT);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_issues, optionsMenu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = optionsMenu.findItem(R.id.m_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        Bundle args = new Bundle();
        args.putParcelable(EXTRA_REPOSITORY, repository);
        searchView.setAppSearchData(args);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isAdded()) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.m_refresh:
                forceRefresh();
                return true;
            case R.id.create_issue:
                startActivityForResult(EditIssueActivity.createIntent(repository),
                        ISSUE_CREATE);
                return true;
            case R.id.m_filter:
                startActivityForResult(
                        EditIssuesFilterActivity.createIntent(filter),
                        ISSUE_FILTER_EDIT);
                return true;
            case R.id.m_bookmark:
                cache.addIssueFilter(filter)
                        .subscribe(response -> ToastUtils.show(getActivity(), R.string.message_filter_saved));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == ISSUE_FILTER_EDIT
                && data != null) {
            IssueFilter newFilter = data
                    .getParcelableExtra(EXTRA_ISSUE_FILTER);
            if (!filter.equals(newFilter)) {
                filter = newFilter;
                getMainSection().setHeader(new IssueFilterHeaderItem(avatars, filter));
                refreshWithProgress();
                return;
            }
        }

        if (requestCode == ISSUE_VIEW) {
            notifyDataSetChanged();
            forceRefresh();
            return;
        }

        if (requestCode == ISSUE_CREATE && resultCode == RESULT_OK) {
            Issue created = data.getParcelableExtra(EXTRA_ISSUE);
            forceRefresh();
            startActivityForResult(
                    IssuesViewActivity.createIntent(created, repository),
                    ISSUE_VIEW);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected Single<Response<Page<Issue>>> loadData(int page) {
        return service.getRepositoryIssues(repository.owner().login(),
                repository.name(), filter.toFilterMap(), page);
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
        return new IssueItem(avatars, item);
    }
}
