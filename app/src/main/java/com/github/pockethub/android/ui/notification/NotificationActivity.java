package com.github.pockethub.android.ui.notification;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.github.pockethub.android.ui.TabPagerActivity;

public class NotificationActivity extends TabPagerActivity<NotificationPagerAdapter> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        configureTabPager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected NotificationPagerAdapter createAdapter() {
        return new NotificationPagerAdapter(this);
    }
}
