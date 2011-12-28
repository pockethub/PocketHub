package com.github.mobile.android.repo;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
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
import com.google.inject.Inject;

import java.io.File;
import java.util.ArrayList;
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

    private class RepoAdapter extends ArrayAdapter<Repository> {

        public RepoAdapter(Repository[] objects) {
            super(RepoBrowseActivity.this, layout.repo_list_item, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LinearLayout view = (LinearLayout) RepoBrowseActivity.this.getLayoutInflater().inflate(
                    layout.repo_list_item, null);
            Repository repo = getItem(position);
            if (user.getLogin().equals(repo.getOwner().getLogin()))
                ((TextView) view.findViewById(id.tv_repo_name)).setText(repo.getName());
            else
                ((TextView) view.findViewById(id.tv_repo_name)).setText(repo.generateId());
            return view;
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
            recentArea.setVisibility(ViewGroup.GONE);

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

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void loadRepos() {
        RequestFuture<List<Repository>> callback = new RequestFuture<List<Repository>>() {

            public void success(List<Repository> repos) {
                if (!recentRepos.isEmpty()) {
                    List<Repository> recent = new ArrayList<Repository>(recentRepos.size());
                    for (Repository repo : repos) {
                        if (recentRepos.contains(repo.generateId()))
                            recent.add(repo);
                        if (recent.size() == recentRepos.size())
                            break;
                    }
                    if (!recent.isEmpty()) {
                        recentList.setAdapter(new RepoAdapter(recent.toArray(new Repository[recent.size()])));
                        recentArea.setVisibility(ViewGroup.VISIBLE);
                    }
                }
                repoList.setAdapter(new RepoAdapter(repos.toArray(new Repository[repos.size()])));
            }
        };
        cache.getRepos(user, callback);
    }
}
