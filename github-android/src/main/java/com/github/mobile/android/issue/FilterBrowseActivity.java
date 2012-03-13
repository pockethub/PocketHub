package com.github.mobile.android.issue;

import android.os.Bundle;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

/**
 * Activity to browse a list of bookmarked {@link IssueFilter} items
 */
public class FilterBrowseActivity extends RoboSherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(string.saved_filters_title);
        setContentView(layout.issue_filter_list);
    }
}
