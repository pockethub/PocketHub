package com.github.mobile.android.repo;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import static com.google.common.collect.Lists.newArrayList;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.async.AuthenticatedUserLoader;
import com.github.mobile.android.issue.IssueBrowseActivity;
import com.github.mobile.android.persistence.AccountDataManager;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
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

    private RecentReposHelper recent;

    @Inject
    private AccountDataManager cache;

    @InjectExtra(EXTRA_USER)
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recent = new RecentReposHelper(getActivity(), user);
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
        recent.add(repo);
        startActivity(IssueBrowseActivity.createIntent(repo));
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    public void onStop() {
        super.onStop();

        recent.save();
    }

    @Override
    public Loader<List<Repository>> onCreateLoader(int id, Bundle args) {
        return new AuthenticatedUserLoader<List<Repository>>(getActivity()) {

            public List<Repository> load() {
                try {
                    Log.d(TAG, "Going to load repos for " + user.getLogin());
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
