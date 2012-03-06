package com.github.mobile.android.issue;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static com.github.mobile.android.issue.ViewIssueActivity.viewIssueIntentFor;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE_FILTER;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY;
import android.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.AccountDataManager;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.RequestFuture;
import com.github.mobile.android.repo.RepoBrowseActivity;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectExtra;

/**
 * Activity for browsing a list of issues
 */
public class IssueBrowseActivity extends RoboFragmentActivity implements OnItemClickListener {

    private static final int CODE_FILTER = 1;

    private static final int CODE_VIEW = 2;

    private static final int CODE_CREATE = 3;

    /**
     * Create intent to browse a repository's issues
     *
     * @param repository
     * @return intent
     */
    public static Intent createIntent(Repository repository) {
        return new Builder("repo.issues.VIEW").repo(repository).toIntent();
    }

    /**
     * Create intent to browse the filtered issues
     *
     * @param filter
     * @return intent
     */
    public static Intent createIntent(IssueFilter filter) {
        return new Builder("repo.issues.VIEW").repo(filter.getRepository()).add(EXTRA_ISSUE_FILTER, filter).toIntent();
    }

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repo;

    @Inject
    private AccountDataManager cache;

    @Inject
    private AvatarHelper avatarHelper;

    @InjectExtra(value = EXTRA_ISSUE_FILTER, optional = true)
    private IssueFilter filter;

    private IssuesFragment issues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.repo_issue_list);
        setTitle(string.issues_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((TextView) findViewById(id.tv_repo_name)).setText(repo.getName());
        ((TextView) findViewById(id.tv_owner_name)).setText(repo.getOwner().getLogin() + " /");
        avatarHelper.bind((ImageView) findViewById(id.iv_gravatar), repo.getOwner());

        if (filter == null)
            filter = new IssueFilter(repo);

        updateFilterSummary();

        issues = (IssuesFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        if (issues == null) {
            issues = new IssuesFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.list, issues).commit();
        }
        issues.setFilter(filter).setRepository(repo).setClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.issues, options);
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
            startActivityForResult(
                    CreateIssueActivity.createIntent(new RepositoryId(repo.getOwner().getLogin(), repo.getName())),
                    CODE_CREATE);
            return true;
        case id.filter_issues:
            startActivityForResult(FilterIssuesActivity.createIntent(repo, filter), CODE_FILTER);
            return true;
        case id.bookmark_filter:
            cache.addIssueFilter(filter, new RequestFuture<IssueFilter>() {

                public void success(IssueFilter response) {
                    Toast.makeText(IssueBrowseActivity.this, "Issue filter saved to bookmarks", LENGTH_LONG).show();
                }
            });
            return true;
        case android.R.id.home:
            Intent intent = RepoBrowseActivity.createIntent(repo.getOwner());
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
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
        if (requestCode == CODE_VIEW) {
            ListAdapter adapter = issues.getListAdapter();
            if (adapter instanceof BaseAdapter)
                ((BaseAdapter) adapter).notifyDataSetChanged();
        }
        if (requestCode == CODE_CREATE && resultCode == CreateIssueActivity.RESULT_CREATED)
            issues.refresh();
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onItemClick(AdapterView<?> list, View view, int position, long id) {
        Issue issue = (Issue) list.getItemAtPosition(position);
        startActivityForResult(viewIssueIntentFor(issue), CODE_VIEW);
    }
}
