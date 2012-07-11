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
import com.github.mobile.core.repo.RefreshRepositoryTask;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemListFragment;
import com.github.mobile.ui.ItemView;
import com.google.inject.Inject;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Fragment to display a list of {@link SearchRepository} instances
 */
public class SearchRepositoryListFragment extends
        ItemListFragment<SearchRepository> {

    @Inject
    private RepositoryService service;

    private String query;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_repositories);
    }

    /**
     * @param query
     * @return this fragment
     */
    public SearchRepositoryListFragment setQuery(final String query) {
        this.query = query;
        return this;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final SearchRepository result = (SearchRepository) l
                .getItemAtPosition(position);
        new RefreshRepositoryTask(getActivity(), result) {

            @Override
            public void execute() {
                showIndeterminate(MessageFormat.format(
                        getString(string.opening_repository),
                        result.generateId()));

                super.execute();
            }

            @Override
            protected void onSuccess(Repository repository) throws Exception {
                super.onSuccess(repository);

                startActivity(RepositoryViewActivity.createIntent(repository));
            }
        }.execute();
    }

    @Override
    public Loader<List<SearchRepository>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<SearchRepository>>(getActivity(), items) {

            @Override
            public List<SearchRepository> loadData() throws Exception {
                return service.searchRepositories(query);
            }
        };
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return string.error_repos_load;
    }

    @Override
    protected ItemListAdapter<SearchRepository, ? extends ItemView> createAdapter(
            List<SearchRepository> items) {
        return new SearchRepositoryListAdapter(getActivity()
                .getLayoutInflater(), items.toArray(new SearchRepository[items
                .size()]));
    }
}
