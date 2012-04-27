package com.github.mobile.repo;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.IRepositorySearch;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.async.AuthenticatedUserTask;
import com.github.mobile.ui.ListLoadingFragment;
import com.github.mobile.ui.repo.RepositoryViewActivity;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Fragment to display a list of {@link Repository} instances
 */
public class SearchRepoListFragment extends ListLoadingFragment<SearchRepository> {

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
        return new ThrowableLoader<List<SearchRepository>>(getActivity(), listItems) {

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
    protected ViewHoldingListAdapter<SearchRepository> adapterFor(List<SearchRepository> items) {
        return new ViewHoldingListAdapter<SearchRepository>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.repo_search_list_item), ReflectiveHolderFactory.reflectiveFactoryFor(SearchRepoViewHolder.class));
    }
}
