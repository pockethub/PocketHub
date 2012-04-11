package com.github.mobile.android.ui.issue;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUES;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_POSITION;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
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
    public static Intent createIntent(Collection<Issue> issues, int position) {
        return new Builder("issues.VIEW").add(EXTRA_ISSUES, new ArrayList<Issue>(issues)).add(EXTRA_POSITION, position)
                .toIntent();
    }

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @InjectExtra(EXTRA_ISSUES)
    private ArrayList<Issue> issues;

    @InjectExtra(EXTRA_POSITION)
    private int initialPosition;

    private IssuesPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager);

        adapter = new IssuesPagerAdapter(getSupportFragmentManager(), issues.toArray(new Issue[issues.size()]));
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
        pager.setCurrentItem(initialPosition);
        onPageSelected(initialPosition);
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Intentionally left blank
    }

    public void onPageSelected(int position) {
        Issue issue = issues.get(position);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(string.issue_title) + Integer.toString(issue.getNumber()));
        RepositoryId repo = RepositoryId.createFromUrl(issue.getHtmlUrl());
        if (repo != null)
            actionBar.setSubtitle(repo.generateId());
        else
            actionBar.setSubtitle(null);
    }

    public void onPageScrollStateChanged(int state) {
        // Intentionally left blank
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        adapter.onDialogResult(pager.getCurrentItem(), requestCode, resultCode, arguments);
    }
}
