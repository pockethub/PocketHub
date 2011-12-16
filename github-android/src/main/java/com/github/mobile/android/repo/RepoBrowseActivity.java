package com.github.mobile.android.repo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.R;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.util.Avatar;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.RepositoryService;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity for browsing repositories associated with a user
 */
public class RepoBrowseActivity extends RoboActivity {

    /**
     * Create intent to show repositories for a user
     *
     * @param context
     * @param user
     * @return intent
     */
    public static Intent createIntent(Context context, User user) {
        Intent intent = new Intent(context, RepoBrowseActivity.class);
        intent.putExtra("user", user);
        return intent;
    }

    private class RepoAdapter extends ArrayAdapter<Repository> {

        public RepoAdapter(Repository[] objects) {
            super(RepoBrowseActivity.this, R.layout.repo_list_item, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LinearLayout view = (LinearLayout) RepoBrowseActivity.this.getLayoutInflater().inflate(
                    layout.repo_list_item, null);
            Repository repo = getItem(position);
            if (user.getLogin().equals(repo.getOwner().getLogin()))
                ((TextView) view.findViewById(R.id.tv_repo_name)).setText(repo.getName());
            else
                ((TextView) view.findViewById(R.id.tv_repo_name)).setText(repo.generateId());
            return view;
        }
    }

    @InjectView(R.id.lv_repos)
    private ListView repoList;

    @Inject
    private RepositoryService repoService;

    private User user;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repo_list);
        user = (User) getIntent().getSerializableExtra("user");

        ((TextView) findViewById(R.id.tv_org_name)).setText(user.getLogin());
        Avatar.bind(this, ((ImageView) findViewById(R.id.iv_gravatar)), user.getLogin(), user.getAvatarUrl());

        loadRepos();
    }

    private void loadRepos() {
        new RoboAsyncTask<Repository[]>(this) {

            public Repository[] call() throws Exception {
                List<Repository> repoList;
                if (!"User".equals(user.getType()))
                    repoList = repoService.getOrgRepositories(user.getLogin());
                else if (user.getLogin().equals(repoService.getClient().getUser()))
                    repoList = repoService.getRepositories();
                else
                    repoList = repoService.getRepositories(user.getLogin());
                Repository[] repos = repoList.toArray(new Repository[repoList.size()]);
                Arrays.sort(repos, new Comparator<Repository>() {

                    public int compare(Repository r1, Repository r2) {
                        return r2.getUpdatedAt().compareTo(r1.getUpdatedAt());
                    }
                });
                return repos;
            }

            protected void onSuccess(Repository[] repos) throws Exception {
                repoList.setAdapter(new RepoAdapter(repos));
            }
        }.execute();
    }
}
