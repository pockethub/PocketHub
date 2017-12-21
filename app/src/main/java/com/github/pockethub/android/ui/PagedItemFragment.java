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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.meisolsson.githubsdk.model.Page;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Response;

/**
 * List fragment that adds more elements when the bottom of the list is scrolled
 * to
 *
 * @param <E>
 */
public abstract class PagedItemFragment<E> extends ItemListFragment<E>
        implements OnScrollListener {

    private ResourceLoadingIndicator loadingIndicator;

    // TODO: Comment
    private int page = 1;
    private boolean hasMore = true;

    /**
     * Get resource id of {@link String} to display when loading
     *
     * @return string resource id
     */
    protected abstract int getLoadingMessage();

    /**
     * Configure list after view has been created
     *
     * @param activity
     * @param listView
     */
    @Override
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

    protected abstract Single<Response<Page<E>>> loadData(int page);

    @Override
    protected Single<List<E>> loadData(boolean forceRefresh) {
        Single<Response<Page<E>>> load = loadData(page);
        page++;
        return load.map(Response::body)
                .map(page -> {
                    hasMore = page.next() != null;
                    return page;
                })
                .map(Page::items);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Intentionally left blank
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (!isUsable()) {
            return;
        }
        if (!hasMore) {
            return;
        }

        if (getLoaderManager().hasRunningLoaders()) {
            return;
        }

        int count = getListAdapter().getWrappedAdapter().getCount();
        if (listView != null && listView.getLastVisiblePosition() >= count) {
            showMore();
        }
    }

    @Override
    protected void forceRefresh() {
        page = 1;
        hasMore = true;

        super.forceRefresh();
    }

    /**
     * Show more events while retaining the current pager state
     */
    private void showMore() {
        refresh();
    }

    @Override
    protected void onDataLoaded(List<E> items) {
        super.onDataLoaded(items);
        loadingIndicator.setVisible(hasMore);
    }

    @Override
    protected void refreshWithProgress() {
        page = 1;
        hasMore = true;

        super.refreshWithProgress();
    }
}
