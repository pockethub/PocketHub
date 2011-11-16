package com.github.mobile.android.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R;
import com.github.mobile.android.views.IssueViewHolder;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;
import roboguice.fragment.RoboListFragment;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;

public abstract class ListLoadingFragment<E> extends RoboListFragment
        implements LoaderManager.LoaderCallbacks<List<E>> {

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText("No data");

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override public void onListItemClick(ListView l, View v, int position, long id) {
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

	abstract ListAdapter adapterFor(List<E> items);

	@Override
	public void onLoaderReset(Loader<List<E>> listLoader) {
		// Clear the data in the adapter.
        // mAdapter.setData(null);
	}
}
