package com.github.mobile.ui;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class PagerFragment extends Fragment implements
    ViewPager.OnPageChangeListener {

    /**
     * Get provider of the currently selected fragment
     *
     * @return fragment provider
     */
    protected abstract FragmentProvider getProvider();

    /**
     * Get selected fragment
     *
     * @return fragment
     */
    protected Fragment getFragment() {
        FragmentProvider provider = getProvider();
        if (provider != null)
            return provider.getSelected();
        else
            return null;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Fragment fragment = getFragment();
        if (fragment != null)
            return fragment.onOptionsItemSelected(item);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Fragment fragment = getFragment();
        if (fragment != null)
            fragment.onCreateOptionsMenu(menu, getActivity().getMenuInflater());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
        int positionOffsetPixels) {
        // Intentionally left blank
    }

    @Override
    public void onPageSelected(int position) {
        // Intentionally left blank
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // Intentionally left blank
    }
}