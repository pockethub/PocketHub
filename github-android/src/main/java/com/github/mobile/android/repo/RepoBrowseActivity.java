package com.github.mobile.android.repo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.filter;
import android.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.RequestReader;
import com.github.mobile.android.RequestWriter;
import com.github.mobile.android.issue.IssueBrowseActivity;
import com.github.mobile.android.util.Avatar;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.google.common.base.Predicate;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity for browsing repositories associated with a user
 */
public class RepoBrowseActivity extends RoboFragmentActivity implements LoaderCallbacks<List<Repository>> {

    private static final int VERSION_RECENT_REPOS = 1;

    private static final String FILE_RECENT_REPOS = "recent_repos.ser";

    private static final int MAX_RECENT_REPOS = 3;

    /**
     * Create intent to show repositories for a user
     *
     * @param user
     * @return intent
     */
    public static Intent createIntent(User user) {
        return new Builder("repos.VIEW").user(user).toIntent();
    }

    @InjectView(id.ll_recent_repos)
    private LinearLayout recentArea;

    @InjectView(id.lv_recent_repos)
    private ListView recentList;

    private LinkedList<String> recentRepos;

    @InjectExtra(EXTRA_USER)
    private User user;

    private RepoListFragment repoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.repo_list);

        ((TextView) findViewById(id.tv_org_name)).setText(user.getLogin());
        Avatar.bind(this, ((ImageView) findViewById(id.iv_gravatar)), user);

        recentRepos = new RequestReader(getRecentReposFile(), VERSION_RECENT_REPOS).read();
        if (recentRepos == null)
            recentRepos = new LinkedList<String>();
        recentArea.setVisibility(GONE);

        OnItemClickListener repoClickListener = new OnItemClickListener() {

            public void onItemClick(AdapterView<?> view, View arg1, int position, long id) {
                Repository repo = (Repository) view.getItemAtPosition(position);
                Iterator<String> iter = recentRepos.iterator();
                String repoId = repo.generateId();
                while (iter.hasNext())
                    if (repoId.equals(iter.next()))
                        iter.remove();
                if (recentRepos.size() == MAX_RECENT_REPOS)
                    recentRepos.removeLast();
                recentRepos.addFirst(repoId);
                startActivity(IssueBrowseActivity.createIntent(repo));
            }
        };

        recentList.setOnItemClickListener(repoClickListener);

        repoFragment = (RepoListFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        if (repoFragment == null) {
            repoFragment = new RepoListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.list, repoFragment).commit();
        }
        repoFragment.setCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        repoFragment.refresh();
    }

    private File getRecentReposFile() {
        return getFileStreamPath(user.getLogin() + '_' + FILE_RECENT_REPOS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        new RequestWriter(getRecentReposFile(), VERSION_RECENT_REPOS).write(recentRepos);
    }

    @Override
    public Loader<List<Repository>> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Repository>> loader, List<Repository> items) {
        if (!recentRepos.isEmpty()) {
            List<Repository> recent = copyOf(filter(items, new Predicate<Repository>() {
                public boolean apply(Repository repo) {
                    return recentRepos.contains(repo.generateId());
                }
            }));

            if (!recent.isEmpty()) {
                recentList.setAdapter(new ViewHoldingListAdapter<Repository>(recent, ViewInflator.viewInflatorFor(this,
                        layout.repo_list_item), ReflectiveHolderFactory
                        .reflectiveFactoryFor(RepoViewHolder.class, user)));
                recentArea.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Repository>> loader) {
        // Intentionally left blank
    }
}
