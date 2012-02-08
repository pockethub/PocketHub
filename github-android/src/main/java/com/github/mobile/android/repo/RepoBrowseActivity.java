package com.github.mobile.android.repo;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import android.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.RequestReader;
import com.github.mobile.android.RequestWriter;
import com.github.mobile.android.issue.IssueBrowseActivity;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.google.inject.Inject;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectExtra;

/**
 * Activity for browsing repositories associated with a user
 */
public class RepoBrowseActivity extends RoboFragmentActivity {

    private static final int VERSION_RECENT_REPOS = 2;

    private static final String FILE_RECENT_REPOS = "recent_repos.ser";

    private static final int MAX_RECENT_REPOS = 5;

    /**
     * Create intent to show repositories for a user
     *
     * @param user
     * @return intent
     */
    public static Intent createIntent(User user) {
        return new Builder("repos.VIEW").user(user).toIntent();
    }

    private LinkedHashSet<String> recentRepos;

    @InjectExtra(EXTRA_USER)
    private User user;

    @Inject
    private AvatarHelper avatarHelper;

    private RepoListFragment repoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.repo_list);
        setTitle(string.repositories_title);

        ((TextView) findViewById(id.tv_org_name)).setText(user.getLogin());
        avatarHelper.bind(((ImageView) findViewById(id.iv_gravatar)), user);

        recentRepos = new RequestReader(getRecentReposFile(), VERSION_RECENT_REPOS).read();
        if (recentRepos == null)
            recentRepos = new LinkedHashSet<String>();

        OnItemClickListener repoClickListener = new OnItemClickListener() {

            public void onItemClick(AdapterView<?> view, View arg1, int position, long id) {
                Repository repo = (Repository) view.getItemAtPosition(position);
                Iterator<String> iter = recentRepos.iterator();
                String repoId = repo.generateId();
                while (iter.hasNext())
                    if (repoId.equals(iter.next()))
                        iter.remove();
                if (recentRepos.size() == MAX_RECENT_REPOS)
                    while (iter.hasNext()) {
                        iter.next();
                        if (!iter.hasNext())
                            iter.remove();
                    }
                recentRepos.add(repoId);
                startActivity(IssueBrowseActivity.createIntent(repo));
            }
        };

        repoFragment = (RepoListFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        if (repoFragment == null) {
            repoFragment = new RepoListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.list, repoFragment).commit();
        }
        repoFragment.setRecent(recentRepos).setClickListener(repoClickListener);
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
}