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

/**
 * List adapter for items of a specific type
 *
 * @param <I>
 * @param <V>
 */
public abstract class ItemListAdapter<I, V extends ItemView> extends BaseAdapter {

    private final LayoutInflater inflater;

    private final int viewId;

    private Object[] elements;

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
     * @param elements
     */
    public ItemListAdapter(final int viewId, final LayoutInflater inflater, final I[] elements) {
        this.viewId = viewId;
        this.inflater = inflater;
        if (elements != null)
            this.elements = elements;
        else
            this.elements = new Object[0];
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * @return items
     */
    @SuppressWarnings("unchecked")
    protected I[] getItems() {
        return (I[]) elements;
    }

    public int getCount() {
        return elements.length;
    }

    @SuppressWarnings("unchecked")
    public I getItem(int position) {
        return (I) elements[position];
    }

    public long getItemId(int position) {
        return elements[position].hashCode();
    }

    /**
     * Set items
     *
     * @param items
     * @return items
     */
    public ItemListAdapter<I, V> setItems(final Object[] items) {
        if (items != null)
            elements = items;
        else
            elements = new Object[0];
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

    @SuppressWarnings("unchecked")
    public View getView(final int position, View convertView, final ViewGroup parent) {
        V view = convertView != null ? (V) convertView.getTag() : null;
        if (view == null) {
            convertView = inflater.inflate(viewId, null);
            view = createView(convertView);
            convertView.setTag(view);
        }
        update(position, view, getItem(position));
        return convertView;
    }
}
