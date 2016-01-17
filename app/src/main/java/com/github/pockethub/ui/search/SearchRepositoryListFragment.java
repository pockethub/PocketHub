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
package com.github.pockethub.ui.search;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.client.GithubListClient;
import com.alorma.github.sdk.services.repo.GetRepoClient;
import com.alorma.github.sdk.services.search.RepoSearchClient;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.core.PageIterator;
import com.github.pockethub.core.ResourcePager;
import com.github.pockethub.core.repo.RefreshRepositoryTask;
import com.github.pockethub.ui.PagedItemFragment;
import com.github.pockethub.ui.repo.RepositoryViewActivity;
import com.github.pockethub.util.InfoUtils;

import java.text.MessageFormat;
import java.util.List;

import static android.app.SearchManager.QUERY;

/**
 * Fragment to display a list of {@link Repo} instances
 */
public class SearchRepositoryListFragment extends PagedItemFragment<Repo> {

    private String query;

    @Override
    protected ResourcePager<Repo> createPager() {
        return new ResourcePager<Repo>() {
            @Override
            protected Object getId(Repo resource) {
                return resource.id;
            }

            @Override
            public PageIterator<Repo> createIterator(int page, int size) {
                return new PageIterator<>(new PageIterator.GitHubRequest<List<Repo>>() {
                    @Override
                    public GithubListClient<List<Repo>> execute(int page) {
                        return new RepoSearchClient(query, page);
                    }
                }, page);
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
        final Repo result = (Repo) l.getItemAtPosition(position);
        new RefreshRepositoryTask(getActivity(), result) {

            @Override
            public void execute() {
                showIndeterminate(MessageFormat.format(
                        getString(R.string.opening_repository),
                        InfoUtils.createRepoId(result)));

                super.execute();
            }

            @Override
            protected void onSuccess(Repo repository) throws Exception {
                super.onSuccess(repository);

                startActivity(RepositoryViewActivity.createIntent(repository));
            }
        }.execute();
    }

    /**
     * Check if the search query is an exact repository name/owner match and
     * open the repository activity and finish the current activity when it is
     *
     * @param query
     * @return true if query opened as repository, false otherwise
     */
    private boolean openRepositoryMatch(final String query) {
        if (TextUtils.isEmpty(query))
            return false;

        Repo repoId = InfoUtils.createRepoFromUrl(query.trim());
        if (repoId == null)
            return false;

        Repo repo;
        repo = new GetRepoClient(InfoUtils.createRepoInfo(repoId)).observable().toBlocking().first();

        startActivity(RepositoryViewActivity.createIntent(repo));
        final Activity activity = getActivity();
        if (activity != null)
            activity.finish();
        return true;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_repos_load;
    }

    @Override
    protected SingleTypeAdapter<Repo> createAdapter(
            List<Repo> items) {
        return new SearchRepositoryListAdapter(getActivity()
                .getLayoutInflater(), items.toArray(new Repo[items
                .size()]));
    }
}
