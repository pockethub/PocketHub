package com.github.mobile.android.ui;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.RefreshAnimation;
import com.github.mobile.android.ThrowableLoader;
import com.github.mobile.android.util.ErrorHelper;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.util.Collections;
import java.util.List;

/**
 * List fragment to load homogeneous items
 *
 * @param <E>
 *            item type
 */
public abstract class ListLoadingFragment<E> extends RoboSherlockListFragment implements LoaderCallbacks<List<E>> {

    private RefreshAnimation refreshAnimation = new RefreshAnimation();

    /**
     * List items provided to {@link #onLoadFinished(Loader, List)}
     */
    protected List<E> listItems = Collections.emptyList();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(menu.refresh, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.refresh:
            refreshAnimation.setRefreshItem(item);
            refresh();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Refresh the fragment's list
     */
    public void refresh() {
        final Activity activity = getActivity();
        if (activity == null)
            return;
        if (getLoaderManager().hasRunningLoaders())
            return;

        refreshAnimation.start(activity);

        getLoaderManager().restartLoader(0, null, this);
    }

    public void onLoadFinished(Loader<List<E>> loader, List<E> items) {
        listItems = items;
        @SuppressWarnings("unchecked")
        ViewHoldingListAdapter<E> adapter = (ViewHoldingListAdapter<E>) getListAdapter();
        if (adapter == null)
            setListAdapter(adapterFor(items));
        else
            adapter.setList(items);

        showList();
    }

    /**
     * Set the list to be shown and stop the refresh animation
     */
    protected void showList() {
        if (isResumed())
            setListShown(true);
        else
            setListShownNoAnimation(true);

        refreshAnimation.stop();
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

    /**
     * Get exception from loader if it provides one by being a {@link ThrowableLoader}
     *
     * @param loader
     * @return exception or null if none provided
     */
    protected Exception getException(final Loader<List<E>> loader) {
        if (loader instanceof ThrowableLoader)
            return ((ThrowableLoader<List<E>>) loader).clearException();
        else
            return null;
    }
}
