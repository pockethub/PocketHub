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
import com.github.mobile.android.issue.IssueBrowseActivity;
import com.github.mobile.android.util.Avatar;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Activity for browsing repositories associated with a user
 */
public class RepoBrowseActivity extends RoboActivity {

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

    @InjectView(id.lv_repos)
    private ListView repoList;

    @Inject
    private AccountDataManager cache;

    private User user;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.repo_list);
        user = (User) getIntent().getSerializableExtra(EXTRA_USER);

        ((TextView) findViewById(id.tv_org_name)).setText(user.getLogin());
        Avatar.bind(this, ((ImageView) findViewById(id.iv_gravatar)), user);

        repoList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> view, View arg1, int position, long id) {
                Repository repo = (Repository) view.getItemAtPosition(position);
                startActivity(IssueBrowseActivity.createIntent(repo));
            }
        });

        loadRepos();
    }

    private void loadRepos() {
        RequestFuture<List<Repository>> callback = new RequestFuture<List<Repository>>() {

            public void success(List<Repository> repos) {
                repoList.setAdapter(new RepoAdapter(repos.toArray(new Repository[repos.size()])));
            }
        };
        cache.getRepos(user, callback);
    }
}
