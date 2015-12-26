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
package com.github.pockethub.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.github.pockethub.ThrowableLoader;
import com.github.pockethub.core.ResourcePager;

import java.io.IOException;
import java.util.List;

/**
 * List fragment that adds more elements when the bottom of the list is scrolled
 * to
 *
 * @param <E>
 */
public abstract class PagedItemFragment<E> extends ItemListFragment<E>
        implements OnScrollListener {

    /**
     * Resource pager
     */
    protected ResourcePager<E> pager;

    private ResourceLoadingIndicator loadingIndicator;

    /**
     * Create pager that provides resources
     *
     * @return pager
     */
    protected abstract ResourcePager<E> createPager();

    /**
     * Get resource id of {@link String} to display when loading
     *
     * @return string resource id
     */
    protected abstract int getLoadingMessage();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pager = createPager();
    }

    /**
     * Configure list after view has been created
     *
     * @param activity
     * @param listView
     */
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        loadingIndicator = new ResourceLoadingIndicator(activity,
                getLoadingMessage());
        loadingIndicator.setList(getListAdapter());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnScrollListener(this);

        getListView().setFastScrollEnabled(true);
    }

    @Override
    public Loader<List<E>> onCreateLoader(int id, Bundle bundle) {
        return new ThrowableLoader<List<E>>(getActivity(), items) {

            @Override
            public List<E> loadData() throws IOException {
                pager.next();
                return pager.getResources();
            }
        };
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Intentionally left blank
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (!isUsable())
            return;
        if (!pager.hasMore())
            return;
        if (getLoaderManager().hasRunningLoaders())
            return;
        if (listView != null
                && listView.getLastVisiblePosition() >= pager.size())
            showMore();
    }

    @Override
    protected void forceRefresh() {
        pager.clear();

        super.forceRefresh();
    }

    /**
     * Show more events while retaining the current pager state
     */
    private void showMore() {
        refresh();
    }

    @Override
    public void onLoadFinished(Loader<List<E>> loader, List<E> items) {
        loadingIndicator.setVisible(pager.hasMore());

        super.onLoadFinished(loader, items);
    }

    @Override
    protected void refreshWithProgress() {
        pager.reset();
        pager = createPager();

        super.refreshWithProgress();
    }
}
