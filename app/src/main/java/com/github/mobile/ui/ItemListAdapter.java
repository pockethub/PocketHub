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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.Collections;
import java.util.List;

/**
 * List adapter for items of a specific type
 *
 * @param <I>
 * @param <V>
 */
public abstract class ItemListAdapter<I, V extends ItemView> extends
        BaseAdapter implements Filterable {

    private final LayoutInflater inflater;

    private final int viewId;

    private List<I> items;

    private List<I> initialItems;

    /**
     * Create empty adapter
     *
     * @param viewId
     * @param inflater
     */
    public ItemListAdapter(final int viewId, final LayoutInflater inflater) {
        this(viewId, inflater, null);
    }

    /**
     * Create adapter
     *
     * @param viewId
     * @param inflater
     * @param items
     */
    public ItemListAdapter(final int viewId, final LayoutInflater inflater,
            final List<I> items) {
        this.viewId = viewId;
        this.inflater = inflater;
        if (items != null)
            this.items = items;
        else
            this.items = Collections.emptyList();
        this.initialItems = this.items;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * Get items being displayed
     *
     * @return items
     */
    public List<I> getItems() {
        return items;
    }

    public int getCount() {
        return items.size();
    }

    public I getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    /**
     * @return initialItems
     */
    protected List<I> getInitialItems() {
        return initialItems;
    }

    /**
     * Set items
     *
     * @param items
     * @return this adapter
     */
    public ItemListAdapter<I, V> setItems(final List<I> items) {
        if (items != null)
            this.items = items;
        else
            this.items = Collections.emptyList();
        initialItems = this.items;
        notifyDataSetChanged();
        return this;
    }

    /**
     * Set filtered items to display
     *
     * @param items
     * @return this adapter
     */
    public ItemListAdapter<I, V> setFilteredItems(final List<I> items) {
        if (items != null)
            this.items = items;
        else
            this.items = Collections.emptyList();
        notifyDataSetChanged();
        return this;
    }

    /**
     * Update view to display item
     *
     * @param position
     * @param view
     * @param item
     */
    protected abstract void update(int position, V view, I item);

    /**
     * Create empty item view
     *
     * @param view
     * @return item
     */
    protected abstract V createView(View view);

    @Override
    public View getView(final int position, View convertView,
            final ViewGroup parent) {
        @SuppressWarnings("unchecked")
        V view = convertView != null ? (V) convertView.getTag() : null;
        if (view == null) {
            convertView = inflater.inflate(viewId, null);
            view = createView(convertView);
            convertView.setTag(view);
        }
        update(position, view, getItem(position));
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
