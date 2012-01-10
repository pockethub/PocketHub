package com.github.mobile.android.issue;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.android.issue.ViewIssueActivity.viewIssueIntentFor;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE_FILTER;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY;
import android.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.util.Avatar;
import com.github.mobile.android.util.GitHubIntents.Builder;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectExtra;

/**
 * Activity for browsing a list of issues
 */
public class IssueBrowseActivity extends RoboFragmentActivity implements OnItemClickListener,
        LoaderCallbacks<List<Issue>> {

    private static final int CODE_FILTER = 1;

    /**
     * Create intent to browse a repository's issues
     *
     * @param repository
     * @return intent
     */
    public static Intent createIntent(Repository repository) {
        return new Builder("repo.issues.VIEW").repo(repository).toIntent();
    }

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repo;

    private IssueFilter filter;

    private IssuesFragment issues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.repo_issue_list);

        ((TextView) findViewById(id.tv_repo_name)).setText(repo.getName());
        ((TextView) findViewById(id.tv_owner_name)).setText(repo.getOwner().getLogin() + " /");
        Avatar.bind(this, (ImageView) findViewById(id.iv_gravatar), repo.getOwner());

        if (savedInstanceState != null)
            filter = (IssueFilter) savedInstanceState.getSerializable(EXTRA_ISSUE_FILTER);

        if (filter == null)
            filter = new IssueFilter();

        updateFilterSummary();

        issues = (IssuesFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        if (issues == null) {
            issues = new IssuesFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.list, issues).commit();
        }
        issues.setFilter(filter).setRepository(repo).setClickListener(this).setLoadListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(menu.issues, options);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_ISSUE_FILTER, filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.create_issue:
            return true;
        case id.filter_issues:
            startActivityForResult(FilterIssuesActivity.createIntent(repo, filter), CODE_FILTER);
            return true;
        case id.bookmark_filter:
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateFilterSummary() {
        CharSequence display = filter.toDisplay();
        TextView summary = (TextView) findViewById(id.tv_filter_summary);
        if (display.length() > 0) {
            summary.setText(display);
            summary.setVisibility(VISIBLE);
        } else
            summary.setVisibility(GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CODE_FILTER && data != null) {
            filter = ((IssueFilter) data.getSerializableExtra(EXTRA_ISSUE_FILTER)).clone();
            updateFilterSummary();
            issues.setFilter(filter);
            issues.refresh();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onItemClick(AdapterView<?> list, View view, int position, long id) {
        Issue issue = (Issue) list.getItemAtPosition(position);
        startActivity(viewIssueIntentFor(issue));
    }

    public Loader<List<Issue>> onCreateLoader(int arg0, Bundle arg1) {
        return null;
    }

    public void onLoadFinished(Loader<List<Issue>> loader, List<Issue> issues) {
        if (issues.size() != 1)
            ((TextView) findViewById(id.tv_issue_count)).setText(MessageFormat.format("{0} issues", issues.size()));
        else
            ((TextView) findViewById(id.tv_issue_count)).setText("1 issue");
    }

    public void onLoaderReset(Loader<List<Issue>> loader) {
    }
}
