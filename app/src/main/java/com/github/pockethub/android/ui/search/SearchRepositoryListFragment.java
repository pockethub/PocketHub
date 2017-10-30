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
package com.github.pockethub.android.ui.search;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.github.pockethub.android.rx.RxProgress;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.core.ResourcePager;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.ui.repo.RepositoryViewActivity;
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.model.SearchPage;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;
import com.meisolsson.githubsdk.service.search.SearchService;

import java.text.MessageFormat;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static android.app.SearchManager.QUERY;

/**
 * Fragment to display a list of {@link Repository} instances
 */
public class SearchRepositoryListFragment extends PagedItemFragment<Repository> {

    private String query;

    @Override
    protected ResourcePager<Repository> createPager() {
        return new ResourcePager<Repository>() {
            @Override
            protected Object getId(Repository resource) {
                return resource.id();
            }

            @Override
            public PageIterator<Repository> createIterator(int page, int size) {
                return new PageIterator<>(page1 ->
                        ServiceGenerator.createService(getContext(), SearchService.class)
                                .searchRepositories(query, null, null, page1)
                                .map(response -> {
                                    SearchPage<Repository> repositorySearchPage = response.body();

                                    return Response.success(Page.<Repository>builder()
                                            .first(repositorySearchPage.first())
                                            .last(repositorySearchPage.last())
                                            .next(repositorySearchPage.next())
                                            .prev(repositorySearchPage.prev())
                                            .items(repositorySearchPage.items())
                                            .build());
                                }), page);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_repositories;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_repositories);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        start();
    }

    @Override
    public void refresh() {
        start();
        super.refresh();
    }

    private void start(){
        query = getStringExtra(QUERY);
        openRepositoryMatch(query);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Repository result = (Repository) l.getItemAtPosition(position);
        ServiceGenerator.createService(getContext(), RepositoryService.class)
                .getRepository(result.owner().login(), result.name())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .compose(RxProgress.bindToLifecycle(getActivity(),
                        MessageFormat.format(getString(R.string.opening_repository),
                                InfoUtils.createRepoId(result))))
                .subscribe(response ->
                        startActivity(RepositoryViewActivity.createIntent(response.body())));
    }

    /**
     * Check if the search query is an exact repository name/owner match and
     * open the repository activity and finish the current activity when it is
     *
     * @param query
     * @return true if query opened as repository, false otherwise
     */
    private boolean openRepositoryMatch(final String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }

        Repository repoId = InfoUtils.createRepoFromUrl(query.trim());
        if (repoId == null) {
            return false;
        }

        ServiceGenerator.createService(getContext(), RepositoryService.class)
                .getRepository(repoId.owner().login(), repoId.name())
                .subscribe(response -> {
                    if (response.isSuccessful()) {
                        startActivity(RepositoryViewActivity.createIntent(response.body()));
                        final Activity activity = getActivity();
                        if (activity != null) {
                            activity.finish();
                        }
                    }
                });

        return true;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_repos_load;
    }

    @Override
    protected SingleTypeAdapter<Repository> createAdapter(
            List<Repository> items) {
        return new SearchRepositoryListAdapter(getActivity()
                .getLayoutInflater(), items.toArray(new Repository[items
                .size()]));
    }
}
