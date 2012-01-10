package com.github.mobile.android.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.ListAdapter;

import java.util.List;

import roboguice.fragment.RoboListFragment;

public abstract class ListLoadingFragment<E> extends RoboListFragment implements LoaderCallbacks<List<E>> {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText("No data");

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        refresh();
    }

    /**
     * Refresh the fragment's list
     */
    public void refresh() {
        getLoaderManager().restartLoader(0, null, this);
    }

    public void onLoadFinished(Loader<List<E>> loader, List<E> items) {
        setListAdapter(adapterFor(items));

        if (isResumed())
            setListShown(true);
        else
            setListShownNoAnimation(true);
    }

    /**
     * Create adapter for list of items
     *
     * @param items
     * @return list adapter
     */
    protected abstract ListAdapter adapterFor(List<E> items);

    @Override
    public void onLoaderReset(Loader<List<E>> listLoader) {
        // Clear the data in the adapter.
        // mAdapter.setData(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("DUMMY", 0); // https://groups.google.com/d/topic/actionbarsherlock/K0pPsiSKF7U/discussion
    }
}
