package com.github.mobile.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.gist.GistsActivity;
import com.github.mobile.android.issue.FilterBrowseActivity;
import com.github.mobile.android.issue.IssueDashboardActivity;
import com.github.mobile.android.repo.OrgListFragment;
import com.github.mobile.android.ui.ListLoadingFragment;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

/**
 * Home screen activity
 */
public class HomeActivity extends RoboSherlockFragmentActivity {

    private static final int CODE_LOGIN = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getSupportMenuInflater().inflate(menu.welcome, optionMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.dashboard_issues:
            startActivity(new Intent(this, IssueDashboardActivity.class));
            return true;
        case id.gists:
            startActivity(new Intent(this, GistsActivity.class));
            return true;
        case id.search:
            onSearchRequested();
            return true;
        case id.bookmarks:
            startActivity(FilterBrowseActivity.createIntent());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.home);

        if (getSupportFragmentManager().findFragmentById(android.R.id.list) == null)
            getSupportFragmentManager().beginTransaction().add(android.R.id.list, new OrgListFragment()).commit();
    }

    private void loadOrgs() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.list);
        if (fragment instanceof ListLoadingFragment)
            ((ListLoadingFragment<?>) fragment).refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (CODE_LOGIN == requestCode && resultCode == RESULT_OK)
            loadOrgs();
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrgs();
    }
}
