package com.github.mobile.android.ui.fragments;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.Toast;

import com.github.mobile.android.util.ErrorHelper;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.util.List;

import roboguice.fragment.RoboListFragment;

/**
 * List fragment to load homogeneous items
 *
 * @param <E>
 *            item type
 */
public abstract class ListLoadingFragment<E> extends RoboListFragment implements LoaderCallbacks<List<E>> {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Refresh the fragment's list
     */
    public void refresh() {
        if (getActivity() != null && !getLoaderManager().hasRunningLoaders())
            getLoaderManager().restartLoader(0, null, this);
    }

    public void onLoadFinished(Loader<List<E>> loader, List<E> items) {
        @SuppressWarnings("unchecked")
        ViewHoldingListAdapter<E> adapter = (ViewHoldingListAdapter<E>) getListAdapter();
        if (adapter == null)
            setListAdapter(adapterFor(items));
        else
            adapter.setList(items);

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
    protected abstract ViewHoldingListAdapter<E> adapterFor(List<E> items);

    @Override
    public void onLoaderReset(Loader<List<E>> listLoader) {
    }

    /**
     * Show exception using {@link ErrorHelper#show(android.content.Context, Exception, int)}
     * <p>
     * This method ensures the {@link Toast} is displayed on the UI thread and so it may be called from any thread
     *
     * @param e
     * @param defaultMessage
     */
    protected void showError(final Exception e, final int defaultMessage) {
        final Activity activity = getActivity();
        if (activity == null)
            return;
        final Application application = activity.getApplication();
        activity.runOnUiThread(new Runnable() {

            public void run() {
                ErrorHelper.show(application, e, defaultMessage);
            }
        });
    }
}
