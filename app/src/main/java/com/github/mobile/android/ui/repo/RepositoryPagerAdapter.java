package com.github.mobile.android.ui.repo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.mobile.android.issue.IssuesFragment;
import com.viewpagerindicator.TitleProvider;

/**
 * Adapter to view a repository's various pages
 */
public class RepositoryPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

    /**
     * @param fm
     */
    public RepositoryPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public String getTitle(int position) {
        switch (position) {
        case 0:
            return "News";
        case 1:
            return "Issues";
        default:
            return null;
        }
    }

    public Fragment getItem(int position) {
        switch (position) {
        case 0:
            return new RepositoryNewsFragment();
        case 1:
            return new IssuesFragment();
        default:
            return null;
        }
    }

    public int getCount() {
        return 2;
    }
}
