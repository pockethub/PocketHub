package com.github.mobile.android.issue;

import android.os.Bundle;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;

import roboguice.activity.RoboFragmentActivity;

/**
 * Activity to browse a list of bookmarked {@link IssueFilter} items
 */
public class FilterBrowseActivity extends RoboFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(string.filter_issues_title));
        setContentView(layout.issue_filter_list);
    }
}
