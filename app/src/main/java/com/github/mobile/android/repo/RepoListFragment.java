package com.github.mobile.android.repo;

import static com.github.mobile.android.HomeActivity.OrgSelectionListener;
import static com.github.mobile.android.repo.RecentReposHelper.RecentRepos;
import static com.google.common.collect.Lists.newArrayList;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.android.HomeActivity;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.async.AuthenticatedUserLoader;
import com.github.mobile.android.persistence.AccountDataManager;
import com.github.mobile.android.ui.ListLoadingFragment;
import com.github.mobile.android.ui.repo.RepositoryViewActivity;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Fragment to display a list of {@link Repository} instances
 */
public class RepoListFragment extends ListLoadingFragment<Repository> implements OrgSelectionListener {

    private static final String TAG = "RLF";

    @Inject
    private AccountDataManager cache;

    private User org;

    private RecentReposHelper recentReposHelper;
    private AtomicReference<RecentRepos> recentReposRef = new AtomicReference<RecentRepos>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HomeActivity) activity).registerOrgSelectionListener(this);
    }

    @Override
    public void onOrgSelected(User org) {
        this.org = org;
        recentReposHelper = new RecentReposHelper(getActivity());
        hideOldContentAndRefresh();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(string.no_repositories));
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Repository repo = (Repository) list.getItemAtPosition(position);
        recentReposHelper.add(repo);
        startActivity(RepositoryViewActivity.createIntent(repo));
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    public void onStop() {
        super.onStop();

        recentReposHelper.save();
    }

    @Override
    public Loader<List<Repository>> onCreateLoader(int id, Bundle args) {
        return new AuthenticatedUserLoader<List<Repository>>(getActivity()) {

            public List<Repository> load() {
                if (org == null)
                    return Collections.emptyList();
                try {
                    Log.d(TAG, "Going to load repos for " + org.getLogin());
                    RecentRepos recentRepos = recentReposHelper.recentReposFrom(cache.getRepos(org), 5);
                    recentReposRef.set(recentRepos);
                    return recentRepos.fullRepoListHeadedByTopRecents;
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
            ReflectiveHolderFactory.reflectiveFactoryFor(RepoViewHolder.class, org, recentReposRef));
    }

}
