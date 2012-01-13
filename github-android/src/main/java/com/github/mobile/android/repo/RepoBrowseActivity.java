package com.github.mobile.android.repo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.filter;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.AccountDataManager;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.RequestFuture;
import com.github.mobile.android.RequestReader;
import com.github.mobile.android.RequestWriter;
import com.github.mobile.android.issue.IssueBrowseActivity;
import com.github.mobile.android.util.Avatar;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Activity for browsing repositories associated with a user
 */
public class RepoBrowseActivity extends RoboActivity {

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

    private class RepoAdapter extends ViewHoldingListAdapter<Repository> {

        public RepoAdapter(List<Repository> repos, User user) {
            super(repos, viewInflatorFor(RepoBrowseActivity.this, layout.repo_list_item),
                            reflectiveFactoryFor(RepoViewHolder.class, user));
        }
    }

    @InjectView(id.ll_recent_repos)
    private LinearLayout recentArea;

    @InjectView(id.lv_recent_repos)
    private ListView recentList;

    @InjectView(id.lv_repos)
    private ListView repoList;

    private LinkedList<String> recentRepos;

    @Inject
    private AccountDataManager cache;

    private User user;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.repo_list);
        user = (User) getIntent().getSerializableExtra(EXTRA_USER);

        ((TextView) findViewById(id.tv_org_name)).setText(user.getLogin());
        Avatar.bind(this, ((ImageView) findViewById(id.iv_gravatar)), user);

        recentRepos = new RequestReader(getRecentReposFile(), VERSION_RECENT_REPOS).read();
        if (recentRepos == null)
            recentRepos = new LinkedList<String>();
        if (recentRepos.isEmpty())
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
        repoList.setOnItemClickListener(repoClickListener);
    }

    protected void onResume() {
        super.onResume();
        loadRepos();
    }

    private File getRecentReposFile() {
        return getFileStreamPath(user.getLogin() + '_' + FILE_RECENT_REPOS);
    }

    protected void onStop() {
        super.onStop();
        new RequestWriter(getRecentReposFile(), VERSION_RECENT_REPOS).write(recentRepos);
    }

    private void loadRepos() {
        RequestFuture<List<Repository>> callback = new RequestFuture<List<Repository>>() {
            public void success(List<Repository> repos) {
                if (!recentRepos.isEmpty()) {
                    List<Repository> recent = copyOf(filter(repos, new Predicate<Repository>() {
                        public boolean apply(Repository repo) { return recentRepos.contains(repo.generateId()); }
                    }));

                    if (!recent.isEmpty()) {
                        recentList.setAdapter(new RepoAdapter(recent, user));
                        recentArea.setVisibility(VISIBLE);
                    }
                }
                repoList.setAdapter(new RepoAdapter(repos, user));
            }
        };
        cache.getRepos(user, callback);
    }
}
