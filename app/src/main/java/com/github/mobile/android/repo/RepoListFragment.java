package com.github.mobile.android.repo;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import static com.google.common.collect.Lists.newArrayList;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.github.mobile.android.persistence.AccountDataManager;
import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectExtra;

/**
 * Fragment to display a list of {@link Repository} instances
 */
public class RepoListFragment extends ListLoadingFragment<Repository> {

    private static final String TAG = "RLF";

    private LoaderCallbacks<List<Repository>> callback;

    private OnItemClickListener clickListener;

    private Collection<String> recent;

    @Inject
    private AccountDataManager cache;

    @InjectExtra(EXTRA_USER)
    private User user;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(string.no_repositories));
        getListView().setFastScrollEnabled(true);
    }

    /**
     * @param recent
     * @return this fragment
     */
    public RepoListFragment setRecent(Collection<String> recent) {
        this.recent = recent;
        return this;
    }

    /**
     * @param clickListener
     * @return this fragment
     */
    public RepoListFragment setClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (clickListener != null)
            clickListener.onItemClick(l, v, position, id);
    }

    /**
     * @param callback
     * @return this fragment
     */
    public RepoListFragment setCallback(LoaderCallbacks<List<Repository>> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void onLoadFinished(Loader<List<Repository>> loader, List<Repository> items) {
        super.onLoadFinished(loader, items);
        if (callback != null)
            callback.onLoadFinished(loader, items);
    }

    @Override
    public Loader<List<Repository>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader<List<Repository>>(getActivity()) {

            public List<Repository> loadInBackground() {
                try {
                    List<Repository> repos = newArrayList(cache.getRepos(user));
                    Collections.sort(repos, new Comparator<Repository>() {

                        public int compare(Repository lhs, Repository rhs) {
                            String lId = lhs.generateId();
                            String rId = rhs.generateId();
                            if (recent.contains(lId) && !recent.contains(rId))
                                return -1;
                            if (recent.contains(rId) && !recent.contains(lId))
                                return 1;

                            return lhs.getName().compareToIgnoreCase(rhs.getName());
                        }
                    });
                    return repos;
                } catch (IOException e) {
                    Log.d(TAG, "Error getting repositories", e);
                    showError(e, string.error_repos_load);
                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<Repository> adapterFor(List<Repository> items) {
        return new ViewHoldingListAdapter<Repository>(items, viewInflatorFor(getActivity(), layout.repo_list_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(RepoViewHolder.class, user, recent));
    }
}
