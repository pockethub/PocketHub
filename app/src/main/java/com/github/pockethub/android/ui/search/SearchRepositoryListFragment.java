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
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.rx.RxProgress;
import com.github.pockethub.android.ui.item.repository.RepositoryItem;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.ui.repo.RepositoryViewActivity;
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.model.SearchPage;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;
import com.meisolsson.githubsdk.service.search.SearchService;
import com.xwray.groupie.Item;

import java.text.MessageFormat;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static android.app.SearchManager.QUERY;

/**
 * Fragment to display a list of {@link Repository} instances
 */
public class SearchRepositoryListFragment extends PagedItemFragment<Repository> {

    SearchService service = ServiceGenerator.createService(getContext(), SearchService.class);

    private String query;

    @Override
    protected Single<Response<Page<Repository>>> loadData(int page) {
        return service.searchRepositories(query, null, null, page)
                .map(response -> {
                    SearchPage<Repository> repositorySearchPage = response.body();

                    return Response.success(Page.<Repository>builder()
                            .first(repositorySearchPage.first())
                            .last(repositorySearchPage.last())
                            .next(repositorySearchPage.next())
                            .prev(repositorySearchPage.prev())
                            .items(repositorySearchPage.items())
                            .build());
                });
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

    private void start() {
        query = getStringExtra(QUERY);
        openRepositoryMatch(query);
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof RepositoryItem) {
            final Repository result = ((RepositoryItem) item).getRepo();
            ServiceGenerator.createService(getContext(), RepositoryService.class)
                    .getRepository(result.owner().login(), result.name())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(RxProgress.bindToLifecycle(getActivity(),
                            MessageFormat.format(getString(R.string.opening_repository),
                                    InfoUtils.createRepoId(result))))
                    .as(AutoDisposeUtils.bindToLifecycle(this))
                    .subscribe(response ->
                            startActivity(RepositoryViewActivity.createIntent(response.body())));
        }
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
    protected int getErrorMessage() {
        return R.string.error_repos_load;
    }

    @Override
    protected Item createItem(Repository item) {
        return new RepositoryItem(item, null);
    }
}
