package com.github.mobile.android;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.TabHost;

import com.github.mobile.android.gist.GistFragment;
import com.github.mobile.android.ui.fragments.IssuesFragment;
import com.github.mobile.android.ui.fragments.PullRequestsFragment;
import com.github.mobile.android.ui.fragments.TabsAdapter;
import roboguice.activity.RoboFragmentActivity;

import static com.github.mobile.android.R.string.issues;
import static com.github.mobile.android.R.string.pull_requests;
import static com.github.mobile.android.R.string.gists;

public class DashboardActivity extends RoboFragmentActivity {
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

		// addTab("news", news, CountingFragment.class);
		addTab("issues", issues, IssuesFragment.class);
		addTab("pulls", pull_requests, PullRequestsFragment.class);
		addTab("gists", gists, GistFragment.class);

        if (savedInstanceState != null) {
            tabHost.setCurrentTabByTag(savedInstanceState.getString(BUNDLE_KEY_TAB));
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
