/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.id;
import com.github.mobile.R.menu;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.util.ToastUtils;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;

import java.util.Collections;
import java.util.List;

/**
 * Base fragment for displaying a list of items that loads with a progress bar visible
 *
 * @param <E>
 */
public abstract class ItemListFragment<E> extends RoboSherlockListFragment implements LoaderCallbacks<List<E>> {

    private static final String FORCE_RELOAD = "force-reload";

    /**
     * List items provided to {@link #onLoadFinished(Loader, List)}
     */
    protected List<E> items = Collections.emptyList();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Start out with a progress bar
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
            forceReload();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * If the user explicitly hits the reload key, they don't want to see cached data. Calling this method means the
     * loader will be passed a 'force-reload' parameter to indicate cached data shouldn't be used and a fresh request
     * should be made.
     */
    protected void forceReload() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_RELOAD, true);
        refresh(bundle);
    }

    /**
     * @param args
     *            the args bundle passed to the loader by the LoaderManager
     * @return true if the bundle indicates the user requested a forced reload of data
     */
    protected static boolean isForcedReload(Bundle args) {
        return args == null ? false : args.getBoolean(FORCE_RELOAD, false);
    }

    /**
     * Refresh the fragment's list
     */
    public void refresh() {
        refresh(null);
    }

    private void refresh(final Bundle args) {
        final Activity activity = getActivity();
        if (activity == null)
            return;

        LoaderManager manager = getLoaderManager();
        if (!manager.hasRunningLoaders())
            manager.restartLoader(0, args, this);
    }

    public void onLoadFinished(Loader<List<E>> loader, List<E> items) {
        this.items = items;
        @SuppressWarnings("rawtypes")
        ItemListAdapter adapter = (ItemListAdapter) getListAdapter();
        if (adapter == null)
            setListAdapter(createAdapter(this.items));
        else
            adapter.setItems(this.items.toArray());

        showList();
    }

    /**
     * Create adapter to display items
     *
     * @param items
     * @return adapter
     */
    protected abstract ItemListAdapter<E, ? extends ItemView> createAdapter(final List<E> items);

    /**
     * Set the list to be shown and stop the refresh animation
     */
    protected void showList() {
        if (isResumed())
            setListShown(true);
        else
            setListShownNoAnimation(true);
    }

    @Override
    public void onLoaderReset(Loader<List<E>> loader) {
        // Intentionally left blank
    }

    /**
     * Show exception in a {@link Toast}
     *
     * @param e
     * @param defaultMessage
     */
    protected void showError(final Exception e, final int defaultMessage) {
        ToastUtils.show(getActivity(), e, defaultMessage);
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

    /**
     * Refresh the list with the progress bar showing
     */
    protected void refreshWithProgress() {
        setListShown(false);
        refresh();
    }
}
