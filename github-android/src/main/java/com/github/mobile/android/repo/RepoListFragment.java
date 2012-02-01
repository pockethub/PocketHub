package com.github.mobile.android.repo;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.ListAdapter;

import com.github.mobile.android.AccountDataManager;
import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.Collections;
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

    @Inject
    private AccountDataManager cache;

    @InjectExtra(EXTRA_USER)
    private User user;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No Repositories");
    }

    /**
     * @param callback
     */
    public void setCallback(LoaderCallbacks<List<Repository>> callback) {
        this.callback = callback;
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
                    return cache.getRepos(user);
                } catch (IOException e) {
                    Log.d(TAG, "Error getting repositories", e);
                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    protected ListAdapter adapterFor(List<Repository> items) {
        return new ViewHoldingListAdapter<Repository>(items, viewInflatorFor(getActivity(), layout.repo_list_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(RepoViewHolder.class, user));
    }
}
