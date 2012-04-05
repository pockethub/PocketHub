package com.github.mobile.android;

import static com.github.mobile.android.R.string.gists;
import static com.github.mobile.android.R.string.issues;
import static com.github.mobile.android.R.string.pull_requests;
import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.gist.GistsActivity;
import com.github.mobile.android.ui.WelcomeActivity;
import com.github.mobile.android.ui.fragments.IssuesFragment;
import com.github.mobile.android.ui.fragments.PullRequestsFragment;
import com.github.mobile.android.ui.fragments.TabsAdapter;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import roboguice.inject.ContextScopedProvider;

/**
 * Main activity
 */
public class DashboardActivity extends RoboSherlockFragmentActivity {

    /**
     * Key to to track current tab
     */
    public static final String BUNDLE_KEY_TAB = "tab";

    private TabHost tabHost;
    private ViewPager viewPager;
    private TabsAdapter tabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_tabs_pager);
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        viewPager = (ViewPager) findViewById(R.id.pager);

        tabsAdapter = new TabsAdapter(this, tabHost, viewPager);
        tabHost.getTabWidget().setDividerDrawable(null);
        addTab("issues", issues, IssuesFragment.class);
        addTab("pulls", pull_requests, PullRequestsFragment.class);
        addTab("gists", gists, GistsActivity.class);

        if (savedInstanceState != null) {
            tabHost.setCurrentTabByTag(savedInstanceState.getString(BUNDLE_KEY_TAB));
        }
    }

    private void addTab(String tag, int indicator, Class<?> clazz) {
        TabSpec spec = tabHost.newTabSpec(tag);
        View view = getLayoutInflater().inflate(R.layout.tab, null);
        spec.setIndicator(view);
        TextView text = (TextView) view.findViewById(id.tv_tab);
        text.setText(getResources().getString(indicator));
        tabsAdapter.addTab(spec, clazz, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_KEY_TAB, tabHost.getCurrentTabTag());
    }
}
