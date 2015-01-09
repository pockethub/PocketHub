package com.github.mobile.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.github.mobile.R;
import com.github.mobile.ui.gist.GistsPagerFragment;
import com.github.mobile.ui.issue.FiltersViewFragment;
import com.github.mobile.ui.issue.IssueDashboardPagerFragment;

/**
 * Created by Henrik on 2015-01-07.
 */
public class TestFragment extends com.github.mobile.ui.roboactivities.RoboActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frament_test);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, new FiltersViewFragment()).commit();
    }
}
