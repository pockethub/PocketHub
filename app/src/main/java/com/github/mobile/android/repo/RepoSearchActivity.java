package com.github.mobile.android.repo;

import static android.app.SearchManager.QUERY;
import static android.content.Intent.ACTION_SEARCH;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static android.widget.Toast.LENGTH_LONG;
import static com.github.mobile.android.repo.RepoSearchRecentSuggestionsProvider.clearRepoQueryHistory;
import static com.github.mobile.android.repo.RepoSearchRecentSuggestionsProvider.saveRecentRepoQuery;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.HomeActivity;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

import java.text.MessageFormat;

/**
 * Activity to search repositories
 */
public class RepoSearchActivity extends RoboSherlockFragmentActivity {

    private SearchRepoListFragment repoFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.repo_search);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(string.repositories_title);
        actionBar.setDisplayHomeAsUpEnabled(true);

        repoFragment = (SearchRepoListFragment) getSupportFragmentManager().findFragmentById(android.R.id.list);
        if (repoFragment == null) {
            repoFragment = new SearchRepoListFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.list, repoFragment).commit();
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        repoFragment.setListShown(false);
        handleIntent(intent);
        repoFragment.refresh();
    }

    private void handleIntent(Intent intent) {
        if (ACTION_SEARCH.equals(intent.getAction()))
            search(intent.getStringExtra(QUERY));
    }

    private void search(final String query) {
        getSupportActionBar().setTitle(MessageFormat.format(getString(string.search_matching), query));
        saveRecentRepoQuery(this, query);
        repoFragment.setQuery(query);
    }
}
