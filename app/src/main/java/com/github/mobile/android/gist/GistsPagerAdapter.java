package com.github.mobile.android.gist;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.mobile.android.R.string;
import com.viewpagerindicator.TitleProvider;

/**
 * Pager adapter for different Gist queries
 */
public class GistsPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

    private final Resources resources;

    /**
     * Create pager adapter
     *
     * @param resources
     * @param fragmentManager
     */
    public GistsPagerAdapter(Resources resources, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.resources = resources;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
        case 0:
            return new MyGistsFragment();
        case 1:
            return new StarredGistsFragment();
        case 2:
            return new PublicGistsFragment();
        default:
            return null;
        }
    }

    @Override
    public String getTitle(int position) {
        switch (position) {
        case 0:
            return resources.getString(string.my_gists_tab);
        case 1:
            return resources.getString(string.starred_gists_tab);
        case 2:
            return resources.getString(string.all_gists_tab);
        default:
            return null;
        }
    }
}
