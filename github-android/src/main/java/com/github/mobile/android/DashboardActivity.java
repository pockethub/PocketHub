package com.github.mobile.android;


import static com.github.mobile.android.R.string.gists;
import static com.github.mobile.android.R.string.issues;
import static com.github.mobile.android.R.string.pull_requests;

import com.github.mobile.android.gist.GistFragment;
import com.github.mobile.android.ui.WelcomeActivity;
import com.github.mobile.android.ui.fragments.IssuesFragment;
import com.github.mobile.android.ui.fragments.PullRequestsFragment;
import com.github.mobile.android.ui.fragments.TabsAdapter;
import com.google.inject.Inject;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.TabHost;
import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContextScopedProvider;

public class DashboardActivity extends RoboFragmentActivity {
    public static final String BUNDLE_KEY_TAB = "tab";

    private TabHost tabHost;
    private ViewPager viewPager;
    private TabsAdapter tabsAdapter;

    @Inject
    ContextScopedProvider<Account> currentAccountProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_tabs_pager);
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        viewPager = (ViewPager) findViewById(R.id.pager);

        tabsAdapter = new TabsAdapter(this, tabHost, viewPager);

        addTab("issues", issues, IssuesFragment.class);
        addTab("pulls", pull_requests, PullRequestsFragment.class);
        addTab("gists", gists, GistFragment.class);

        if (savedInstanceState != null) {
            tabHost.setCurrentTabByTag(savedInstanceState.getString(BUNDLE_KEY_TAB));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentAccountProvider.get(this) == null) {
            startActivityForResult(new Intent(this, WelcomeActivity.class), 0);
        }
    }

    private void addTab(String tag, int indicator, Class<?> clazz) {
        tabsAdapter.addTab(tabHost.newTabSpec(tag).setIndicator(this.getResources().getString(indicator)), clazz, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_KEY_TAB, tabHost.getCurrentTabTag());
    }

}
