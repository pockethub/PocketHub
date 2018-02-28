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

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.meisolsson.githubsdk.model.Page;
import com.xwray.groupie.Item;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Response;

/**
 * List fragment that adds more elements when the bottom of the list is scrolled
 * to.
 */
public abstract class PagedItemFragment<E> extends ItemListFragment<E> {

    private final RecyclerView.OnScrollListener scrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!isUsable()) {
                        return;
                    }
                    if (!hasMore) {
                        return;
                    }

                    // The item count minus the footer
                    int count = getMainSection().getItemCount() - 1;
                    LinearLayoutManager layoutManager = getLayoutManager();

                    if (layoutManager != null) {
                        if (layoutManager.findLastVisibleItemPosition() >= count) {
                            showMore();
                        }
                    }
                }
            };

    private ResourceLoadingIndicator loadingIndicator;

    /**
     * The current page.
     */
    private int page = 1;

    /**
     * Is there more items to fetch.
     */
    private boolean hasMore = true;

    /**
     * Get resource id of {@link String} to display when loading.
     *
     * @return string resource id
     */
    protected abstract int getLoadingMessage();

    @Override
    protected void configureList(RecyclerView recyclerView) {
        super.configureList(recyclerView);
        loadingIndicator = new ResourceLoadingIndicator(getLoadingMessage());
        loadingIndicator.setSection(getMainSection());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getRecyclerView().addOnScrollListener(scrollListener);
    }

    @Override
    public void onDestroyView() {
        getRecyclerView().removeOnScrollListener(scrollListener);
        super.onDestroyView();
    }

    protected abstract Single<Response<Page<E>>> loadData(int page);

    @Override
    protected Single<List<E>> loadData(boolean forceRefresh) {
        return loadData(page)
                .map(Response::body)
                .map(page -> {
                    hasMore = page.next() != null;
                    return page;
                })
                .map(Page::items);

    }

    private void resetPagingData() {
        page = 1;
        hasMore = true;
    }

    @Override
    protected void forceRefresh() {
        resetPagingData();
        loadingIndicator.setVisible(false);
        super.forceRefresh();
    }

    @Override
    public void refresh() {
        refresh(false);
    }

    @Override
    protected void refreshWithProgress() {
        resetPagingData();
        loadingIndicator.setVisible(false);
        super.refreshWithProgress();
    }

    /**
     * Show more events while retaining the current pager state.
     */
    private void showMore() {
        if (isUsable() && !isLoading) {
            page++;
            refresh();
        }
    }

    @Override
    protected void onDataLoaded(List<Item> items) {
        super.onDataLoaded(items);
        loadingIndicator.setVisible(hasMore);
    }
}
