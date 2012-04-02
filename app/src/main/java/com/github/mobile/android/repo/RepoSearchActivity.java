package com.github.mobile.android.repo;

import static android.content.Intent.ACTION_SEARCH;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static android.widget.Toast.LENGTH_LONG;
import static com.github.mobile.android.repo.RepoSearchRecentSuggestionsProvider.clearRepoQueryHistory;
import static com.github.mobile.android.repo.RepoSearchRecentSuggestionsProvider.saveRecentRepoQuery;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.HomeActivity;
import com.github.mobile.android.IRepositorySearch;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.issue.IssueBrowseActivity;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.service.RepositoryService;

import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to search repositories
 */
public class RepoSearchActivity extends RoboSherlockFragmentActivity {

    private class RepoAdapter extends ArrayAdapter<SearchRepository> {

        public RepoAdapter(SearchRepository[] objects) {
            super(RepoSearchActivity.this, layout.repo_list_item, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LinearLayout view = (LinearLayout) RepoSearchActivity.this.getLayoutInflater().inflate(
                    layout.repo_list_item, null);
            SearchRepository repo = getItem(position);
            ((TextView) view.findViewById(id.tv_repo_name)).setText(repo.generateId());
            return view;
        }
    }

    @InjectView(id.lv_repos)
    private ListView repoList;

    @Inject
    private IRepositorySearch search;

    @Inject
    private RepositoryService repos;

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.search, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.search:
            onSearchRequested();
            return true;
        case id.clear_search_history:
            clearRepoQueryHistory(this);
            Toast.makeText(this, string.search_history_cleared, LENGTH_LONG).show();
            return true;
        case android.R.id.home:
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.repo_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        repoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SearchRepository result = (SearchRepository) parent.getItemAtPosition(position);
                new RoboAsyncTask<Repository>(RepoSearchActivity.this) {

                    public Repository call() throws Exception {
                        return repos.getRepository(result);
                    }

                    protected void onSuccess(Repository repository) throws Exception {
                        startActivity(IssueBrowseActivity.createIntent(repository));
                    }
                }.execute();
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }
    }

    private void search(final String query) {
        saveRecentRepoQuery(this, query);
        new RoboAsyncTask<List<SearchRepository>>(this) {

            public List<SearchRepository> call() throws Exception {
                return search.search(query);
            }

            protected void onSuccess(List<SearchRepository> repos) throws Exception {
                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle("“" + query + "”");
                repoList.setAdapter(new RepoAdapter(repos.toArray(new SearchRepository[repos.size()])));
            }
        }.execute();
    }
}
