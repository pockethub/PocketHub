package com.github.mobile.android.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

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
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
    }

    public void onLoadFinished(Loader<List<E>> loader, List<E> items) {
        setListAdapter(adapterFor(items));

        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    protected abstract ListAdapter adapterFor(List<E> items);

    @Override
    public void onLoaderReset(Loader<List<E>> listLoader) {
        // Clear the data in the adapter.
        // mAdapter.setData(null);
    }
}
