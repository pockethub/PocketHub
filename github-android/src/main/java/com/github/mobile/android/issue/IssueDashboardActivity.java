package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.google.inject.Inject;
import com.viewpagerindicator.TitlePageIndicator;

import org.eclipse.egit.github.core.service.IssueService;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectView;

/**
 * Dashboard activity for issues
 */
public class IssueDashboardActivity extends RoboFragmentActivity {

    @InjectView(id.tpi_header)
    private TitlePageIndicator indicator;

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @Inject
    private IssueService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.issue_dashboard);

        pager.setAdapter(new IssueDashboardPagerAdapter(getApplicationContext(), service, getSupportFragmentManager()));
        indicator.setViewPager(pager);
    }
}
