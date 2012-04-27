package com.github.mobile.issue;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.util.GitHubIntents.EXTRA_ISSUE_FILTER;
import static com.github.mobile.util.GitHubIntents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.layout;
import com.github.mobile.util.AvatarHelper;
import com.github.mobile.util.GitHubIntents.Builder;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Repository;

import roboguice.inject.InjectExtra;

/**
 * Activity for browsing a list of issues scoped to a single {@link IssueFilter}
 */
public class IssueBrowseActivity extends RoboSherlockFragmentActivity {

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
    private AvatarHelper avatarHelper;

    private IssuesFragment issues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.repo_issue_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(repo.getName());
        actionBar.setSubtitle(repo.getOwner().getLogin());
        actionBar.setDisplayHomeAsUpEnabled(true);
        avatarHelper.bind(actionBar, repo.getOwner());

        issues = (IssuesFragment) getSupportFragmentManager().findFragmentById(android.R.id.list);
        if (issues == null) {
            issues = new IssuesFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.list, issues).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = FilterBrowseActivity.createIntent();
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
