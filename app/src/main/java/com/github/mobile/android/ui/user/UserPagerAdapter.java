package com.github.mobile.android.ui.user;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.mobile.android.repo.RepoListFragment;
import com.viewpagerindicator.TitleProvider;

/**
 * Pager adapter for a user's different views
 */
public class UserPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

    /**
     * @param fm
     */
    public UserPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
        case 0:
            return new UserNewsFragment();
        case 1:
            return new RepoListFragment();
        default:
            return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public String getTitle(int position) {
        switch (position) {
        case 0:
            return "News";
        case 1:
            return "Repos";
        default:
            return null;
        }
    }
}
