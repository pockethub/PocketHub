package com.github.mobile.android.gist;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST_ID;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Adapter to page through an array of Gists
 */
public class GistsPagerAdapter extends FragmentPagerAdapter {

    private final String[] ids;

    /**
     * @param fm
     * @param gistIds
     */
    public GistsPagerAdapter(FragmentManager fm, String[] gistIds) {
        super(fm);
        this.ids = gistIds;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new GistFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_GIST_ID, ids[position]);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return ids.length;
    }
}
