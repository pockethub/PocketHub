package com.github.mobile.android.repo;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.github.mobile.android.IRepositorySearch;
import com.github.mobile.android.R;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.issue.IssueBrowseActivity;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.service.RepositoryService;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to search repositories
 */
public class RepoSearchActivity extends RoboActivity {

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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repo_search);

        ((EditText) findViewById(R.id.et_search)).setOnEditorActionListener(new OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_SEARCH == actionId)
                    search(v.getText().toString());
                return false;
            }
        });

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
    }

    private void search(final String query) {
        new RoboAsyncTask<List<SearchRepository>>(this) {

            public List<SearchRepository> call() throws Exception {
                return search.search(query);
            }

            protected void onSuccess(List<SearchRepository> repos) throws Exception {
                repoList.setAdapter(new RepoAdapter(repos.toArray(new SearchRepository[repos.size()])));
            }
        }.execute();
    }
}
