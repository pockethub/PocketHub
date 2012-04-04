package com.github.mobile.android.gist;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST_ID;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.eclipse.egit.github.core.Gist;

/**
 * Adapter to page through an array of Gists
 */
public class GistsPagerAdapter extends FragmentPagerAdapter {

    private final Gist[] gists;

    /**
     * @param fm
     * @param gists
     */
    public GistsPagerAdapter(FragmentManager fm, Gist[] gists) {
        super(fm);
        this.gists = gists;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new GistFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_GIST_ID, gists[position].getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return gists.length;
    }
}
