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

import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ListView.FixedViewInfo;

import java.util.ArrayList;

/**
 * Utility adapter that supports adding headers and footers
 *
 * @param <E>
 */
public class HeaderFooterListAdapter<E extends BaseAdapter> extends
        HeaderViewListAdapter {

    private final ListView list;

    private final ArrayList<FixedViewInfo> headers;

    private final ArrayList<FixedViewInfo> footers;

    private final E wrapped;

    /**
     * Create header footer adapter
     *
     * @param view
     * @param adapter
     */
    public HeaderFooterListAdapter(ListView view, E adapter) {
        this(new ArrayList<FixedViewInfo>(), new ArrayList<FixedViewInfo>(),
                view, adapter);
    }

    private HeaderFooterListAdapter(ArrayList<FixedViewInfo> headerViewInfos,
            ArrayList<FixedViewInfo> footerViewInfos, ListView view, E adapter) {
        super(headerViewInfos, footerViewInfos, adapter);

        headers = headerViewInfos;
        footers = footerViewInfos;
        list = view;
        wrapped = adapter;
    }

    /**
     * Add non-selectable header view with no data
     *
     * @see #addHeader(View, Object, boolean)
     * @param view
     * @return this adapter
     */
    public HeaderFooterListAdapter<E> addHeader(View view) {
        return addHeader(view, null, false);
    }

    /**
     * Add header
     *
     * @param view
     * @param data
     * @param isSelectable
     * @return this adapter
     */
    public HeaderFooterListAdapter<E> addHeader(View view, Object data,
            boolean isSelectable) {
        FixedViewInfo info = list.new FixedViewInfo();
        info.view = view;
        info.data = data;
        info.isSelectable = isSelectable;

        headers.add(info);
        wrapped.notifyDataSetChanged();
        return this;
    }

    /**
     * Add non-selectable footer view with no data
     *
     * @see #addFooter(View, Object, boolean)
     * @param view
     * @return this adapter
     */
    public HeaderFooterListAdapter<E> addFooter(View view) {
        return addFooter(view, null, false);
    }

    /**
     * Add footer
     *
     * @param view
     * @param data
     * @param isSelectable
     * @return this adapter
     */
    public HeaderFooterListAdapter<E> addFooter(View view, Object data,
            boolean isSelectable) {
        FixedViewInfo info = list.new FixedViewInfo();
        info.view = view;
        info.data = data;
        info.isSelectable = isSelectable;

        footers.add(info);
        wrapped.notifyDataSetChanged();
        return this;
    }

    @Override
    public boolean removeHeader(View v) {
        boolean removed = super.removeHeader(v);
        if (removed)
            wrapped.notifyDataSetChanged();
        return removed;
    }

    /**
     * Remove all headers
     *
     * @return true if headers were removed, false otherwise
     */
    public boolean clearHeaders() {
        boolean removed = false;
        if (!headers.isEmpty()) {
            FixedViewInfo[] infos = headers.toArray(new FixedViewInfo[headers
                    .size()]);
            for (FixedViewInfo info : infos)
                removed = super.removeHeader(info.view) || removed;
        }
        if (removed)
            wrapped.notifyDataSetChanged();
        return removed;
    }

    /**
     * Remove all footers
     *
     * @return true if headers were removed, false otherwise
     */
    public boolean clearFooters() {
        boolean removed = false;
        if (!footers.isEmpty()) {
            FixedViewInfo[] infos = footers.toArray(new FixedViewInfo[footers
                    .size()]);
            for (FixedViewInfo info : infos)
                removed = super.removeFooter(info.view) || removed;
        }
        if (removed)
            wrapped.notifyDataSetChanged();
        return removed;
    }

    @Override
    public boolean removeFooter(View v) {
        boolean removed = super.removeFooter(v);
        if (removed)
            wrapped.notifyDataSetChanged();
        return removed;
    }

    @Override
    public E getWrappedAdapter() {
        return wrapped;
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }
}
