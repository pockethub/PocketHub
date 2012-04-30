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
package com.github.mobile.ui.repo;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.R.string;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.accounts.AuthenticatedUserTask;
import com.github.mobile.core.repo.IRepositorySearch;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemListFragment;
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Fragment to display a list of {@link Repository} instances
 */
public class SearchRepoListFragment extends ItemListFragment<SearchRepository> {

    @Inject
    private IRepositorySearch search;

    @Inject
    private RepositoryService repos;

    private String query;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(string.no_repositories));
        ListViewUtils.configure(getActivity(), getListView(), true);
    }

    /**
     * @param query
     * @return this fragment
     */
    public SearchRepoListFragment setQuery(final String query) {
        this.query = query;
        return this;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final SearchRepository result = (SearchRepository) l.getItemAtPosition(position);
        new AuthenticatedUserTask<Repository>(getActivity()) {

            public Repository run() throws Exception {
                return repos.getRepository(result);
            }

            protected void onSuccess(Repository repository) throws Exception {
                startActivity(RepositoryViewActivity.createIntent(repository));
            }
        }.execute();
    }

    @Override
    public Loader<List<SearchRepository>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<SearchRepository>>(getActivity(), items) {

            public List<SearchRepository> loadData() throws Exception {
                return search.search(query);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<SearchRepository>> loader, List<SearchRepository> items) {
        Exception exception = getException(loader);
        if (exception != null) {
            showError(exception, string.error_repos_load);
            showList();
            return;
        }

        super.onLoadFinished(loader, items);
    }

    @Override
    protected ItemListAdapter<SearchRepository, ? extends ItemView> createAdapter(List<SearchRepository> items) {
        return new SearchRepositoryListAdapter(getActivity().getLayoutInflater(),
                items.toArray(new SearchRepository[items.size()]));

    }
}
