package com.github.mobile.android.ui.issue;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE_NUMBERS;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_POSITION;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORIES;
import android.R.integer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.UrlLauncher;
import com.github.mobile.android.util.GitHubIntents.Builder;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity display a collection of issues in a pager
 */
public class ViewIssuesActivity extends DialogFragmentActivity implements OnPageChangeListener {

    /**
     * Create an intent to show a single issue
     *
     * @param issue
     * @return intent
     */
    public static Intent createIntent(Issue issue) {
        ArrayList<Issue> list = new ArrayList<Issue>(1);
        list.add(issue);
        return createIntent(list, 0);
    }

    /**
     * Create an intent to show issues with an initial selected issue
     *
     * @param issues
     * @param position
     * @return intent
     */
    public static Intent createIntent(Collection<? extends Issue> issues, int position) {
        ArrayList<Integer> numbers = new ArrayList<Integer>(issues.size());
        ArrayList<RepositoryId> repos = new ArrayList<RepositoryId>(issues.size());
        for (Issue issue : issues) {
            numbers.add(issue.getNumber());
            repos.add(RepositoryId.createFromUrl(issue.getHtmlUrl()));
        }
        return new Builder("issues.VIEW").add(EXTRA_ISSUE_NUMBERS, numbers).add(EXTRA_REPOSITORIES, repos)
                .add(EXTRA_POSITION, position).toIntent();
    }

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @InjectExtra(EXTRA_ISSUE_NUMBERS)
    private ArrayList<integer> issueIds;

    @InjectExtra(EXTRA_REPOSITORIES)
    private ArrayList<RepositoryId> repoIds;

    @InjectExtra(EXTRA_POSITION)
    private int initialPosition;

    private IssuesPagerAdapter adapter;

    private final UrlLauncher urlLauncher = new UrlLauncher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager);

        adapter = new IssuesPagerAdapter(getSupportFragmentManager(),
                repoIds.toArray(new RepositoryId[repoIds.size()]), issueIds.toArray(new Integer[issueIds.size()]));
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
        pager.setCurrentItem(initialPosition);
        onPageSelected(initialPosition);
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Intentionally left blank
    }

    public void onPageSelected(int position) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(string.issue_title) + issueIds.get(position));
        actionBar.setSubtitle(repoIds.get(position).generateId());
    }

    public void onPageScrollStateChanged(int state) {
        // Intentionally left blank
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        adapter.onDialogResult(pager.getCurrentItem(), requestCode, resultCode, arguments);
    }

    @Override
    public void startActivity(Intent intent) {
        Intent converted = urlLauncher.convert(intent);
        if (converted != null)
            super.startActivity(converted);
        else
            super.startActivity(intent);
    }
}
