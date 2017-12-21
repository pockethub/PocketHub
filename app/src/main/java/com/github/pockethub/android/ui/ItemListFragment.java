/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

// TODO: This class should probably just take data as a list.
// All data fetched from an async source should probably be paged. So PagedItemFragment should
// do most of the heavy lifting in the case.

/**
 * Base fragment for displaying a list of items that loads with a progress bar
 * visible
 *
 * @param <E>
 */
public abstract class ItemListFragment<E> extends DialogFragment implements
        SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeLayout;

    /**
     * List items
     */
    protected List<E> items = new ArrayList<>();

    /**
     * List view
     */
    protected ListView listView;

    /**
     * Empty view
     */
    protected TextView emptyView;

    /**
     * Progress bar
     */
    protected ProgressBar progressBar;

    /**
     * Is the list currently shown?
     */
    protected boolean listShown;

    /**
     * Disposable for data load request
     */
    private Disposable dataLoadDisposable;

    protected boolean isLoading;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!items.isEmpty()) {
            setListShown(true, false);
        }

        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    @Override
    public void onRefresh() {
        forceRefresh();
    }

    /**
     * Detach from list view.
     */
    @Override
    public void onDestroyView() {
        listShown = false;
        emptyView = null;
        progressBar = null;
        listView = null;

        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_item);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(
                R.color.pager_title_background_top_start,
                R.color.pager_title_background_end,
                R.color.text_link,
                R.color.pager_title_background_end);

        listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnItemClickListener((parent, view1, position, id) ->
                onListItemClick((ListView) parent, view1, position, id));
        listView.setOnItemLongClickListener((parent, view12, position, id) ->
                onListItemLongClick((ListView) parent, view12, position, id));
        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);

        emptyView = (TextView) view.findViewById(android.R.id.empty);

        configureList(getActivity(), getListView());
    }
    
    /**
     * Configure list after view has been created
     *
     * @param activity
     * @param listView
     */
    protected void configureList(Activity activity, ListView listView) {
        listView.setAdapter(createAdapter());
    }

    /**
     * Force a refresh of the items displayed ignoring any cached items
     */
    protected void forceRefresh() {
        items.clear();
        refresh(true);
    }

    public void refresh() {
        refresh(false);
    }

    /**
     * Refresh the fragment's list
     */
    public void refresh(boolean force) {
        if (!isUsable() || isLoading) {
            return;
        }

        if (dataLoadDisposable != null && !dataLoadDisposable.isDisposed()) {
            dataLoadDisposable.dispose();
        }

        isLoading = true;
        dataLoadDisposable = loadData(force)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onDataLoaded, this::onDataLoadError);
    }

    /**
     * Get error message to display for exception
     *
     * @return string resource id
     */
    protected abstract int getErrorMessage();

    /**
     * Load data async via a Single
     *
     * @return Single to subscribe to
     * @param forceRefresh
     */
    protected abstract Single<List<E>> loadData(boolean forceRefresh);

    /**
     * Called when the data has loaded
     * @param items The items added to the list,
     */
    protected void onDataLoaded(List<E> items) {
        if (!isUsable()) {
            return;
        }

        isLoading = false;
        swipeLayout.setRefreshing(false);

        this.items.addAll(items);
        getListAdapter().getWrappedAdapter().setItems(this.items);
        showList();
    }

    protected void onDataLoadError(Throwable throwable) {
        if (!isUsable()) {
            return;
        }

        isLoading = false;
        swipeLayout.setRefreshing(false);

        if (throwable != null) {
            showError(throwable, getErrorMessage());
            showList();
        }
    }

    /**
     * Create adapter to display items
     *
     * @return adapter
     */
    protected HeaderFooterListAdapter<SingleTypeAdapter<E>> createAdapter() {
        SingleTypeAdapter<E> wrapped = createAdapter(items);
        return new HeaderFooterListAdapter<>(getListView(),
                wrapped);
    }

    /**
     * Create adapter to display items
     *
     * @param items
     * @return adapter
     */
    protected abstract SingleTypeAdapter<E> createAdapter(final List<E> items);

    /**
     * Set the list to be shown
     */
    protected void showList() {
        setListShown(true, isResumed());
    }

    /**
     * Show exception in a {@link Toast}
     *
     * @param e
     * @param defaultMessage
     */
    protected void showError(final Throwable e, final int defaultMessage) {
        ToastUtils.show(getActivity(), e, defaultMessage);
    }

    /**
     * Refresh the list with the progress bar showing
     */
    protected void refreshWithProgress() {
        items.clear();
        setListShown(false);
        refresh();
    }

    /**
     * Get {@link ListView}
     *
     * @return listView
     */
    public ListView getListView() {
        return listView;
    }

    /**
     * Get list adapter
     *
     * @return list adapter
     */
    @SuppressWarnings("unchecked")
    protected HeaderFooterListAdapter<SingleTypeAdapter<E>> getListAdapter() {
        if (listView != null) {
            return (HeaderFooterListAdapter<SingleTypeAdapter<E>>) listView
                    .getAdapter();
        } else {
            return null;
        }
    }

    /**
     * Notify the underlying adapter that the data set has changed
     *
     * @return this fragment
     */
    protected ItemListFragment<E> notifyDataSetChanged() {
        HeaderFooterListAdapter<SingleTypeAdapter<E>> root = getListAdapter();
        if (root != null) {
            SingleTypeAdapter<E> typeAdapter = root.getWrappedAdapter();
            if (typeAdapter != null) {
                typeAdapter.notifyDataSetChanged();
            }
        }
        return this;
    }

    /**
     * Set list adapter to use on list view
     *
     * @param adapter
     * @return this fragment
     */
    protected ItemListFragment<E> setListAdapter(final ListAdapter adapter) {
        if (listView != null) {
            listView.setAdapter(adapter);
        }
        return this;
    }

    private ItemListFragment<E> fadeIn(final View view, final boolean animate) {
        if (view != null) {
            if (animate) {
                view.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                        android.R.anim.fade_in));
            } else {
                view.clearAnimation();
            }
        }
        return this;
    }

    private ItemListFragment<E> show(final View view) {
        view.setVisibility(View.VISIBLE);
        return this;
    }

    private ItemListFragment<E> hide(final View view) {
        view.setVisibility(View.GONE);
        return this;
    }

    /**
     * Set list shown or progress bar show
     *
     * @param shown
     * @return this fragment
     */
    public ItemListFragment<E> setListShown(final boolean shown) {
        return setListShown(shown, true);
    }

    /**
     * Set list shown or progress bar show
     *
     * @param shown
     * @param animate
     * @return this fragment
     */
    public ItemListFragment<E> setListShown(final boolean shown,
            final boolean animate) {
        if (!isUsable()) {
            return this;
        }

        if (shown == listShown) {
            if (shown)
                // List has already been shown so hide/show the empty view with
                // no fade effect
            {
                if (items.isEmpty()) {
                    hide(listView).show(emptyView);
                } else {
                    hide(emptyView).show(listView);
                }
            }
            return this;
        }

        listShown = shown;

        if (shown) {
            if (!items.isEmpty()) {
                hide(progressBar).hide(emptyView).fadeIn(listView, animate)
                        .show(listView);
            } else {
                hide(progressBar).hide(listView).fadeIn(emptyView, animate)
                        .show(emptyView);
            }
        } else {
            hide(listView).hide(emptyView).fadeIn(progressBar, animate)
                    .show(progressBar);
        }

        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param message
     * @return this fragment
     */
    protected ItemListFragment<E> setEmptyText(final String message) {
        if (emptyView != null) {
            emptyView.setText(message);
        }
        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param resId
     * @return this fragment
     */
    protected ItemListFragment<E> setEmptyText(final int resId) {
        if (emptyView != null) {
            emptyView.setText(resId);
        }
        return this;
    }

    /**
     * Callback when a list view item is clicked
     *
     * @param l
     * @param v
     * @param position
     * @param id
     */
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    /**
     * Callback when a list view item is clicked and held
     *
     * @param l
     * @param v
     * @param position
     * @param id
     * @return true if the callback consumed the long click, false otherwise
     */
    public boolean onListItemLongClick(ListView l, View v, int position, long id) {
        return false;
    }
}
