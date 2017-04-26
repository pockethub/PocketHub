package com.github.pockethub.android.ui.notification;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.FragmentPagerAdapter;

class NotificationPagerAdapter extends FragmentPagerAdapter {

    private final AppCompatActivity activity;

    public NotificationPagerAdapter(AppCompatActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new NotificationListFragment();;
        Bundle args = new Bundle();
        switch (position) {
            case 0:
                break;
            case 1:
                args.putString(NotificationListFragment.EXTRA_FILTER, "participating");
                break;
            case 2:
                args.putString(NotificationListFragment.EXTRA_FILTER, "all");
                break;
            default:
                throw new IllegalStateException("Item doesn't exist");
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return activity.getString(R.string.tab_unread);
            case 1:
                return activity.getString(R.string.tab_participating);
            case 2:
                return activity.getString(R.string.tab_all);
            default:
                throw new IllegalStateException("Title doesn't exist");
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
