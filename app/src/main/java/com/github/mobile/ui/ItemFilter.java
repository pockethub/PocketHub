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

import static java.util.Locale.US;
import android.text.TextUtils;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Item filter
 *
 * @param <V>
 */
public abstract class ItemFilter<V> extends Filter {

    private final ItemListAdapter<V, ?> adapter;

    /**
     * Create item filter
     *
     * @param adapter
     */
    public ItemFilter(ItemListAdapter<V, ?> adapter) {
        this.adapter = adapter;
    }

    /**
     * Does the value contain the filter text?
     *
     * @param filter
     * @param value
     * @return true if contains, false otherwise
     */
    protected boolean contains(final String filter, final String value) {
        if (TextUtils.isEmpty(value))
            return false;

        final int fLength = filter.length();
        final int vLength = value.length();
        if (fLength > vLength)
            return false;

        int fIndex = 0;
        int vIndex = 0;
        while (vIndex < vLength)
            if (filter.charAt(fIndex) == Character.toUpperCase(value
                    .charAt(vIndex))) {
                vIndex++;
                fIndex++;
                if (fIndex == fLength)
                    return true;
            } else {
                vIndex += fIndex + 1;
                fIndex = 0;
            }
        return false;
    }

    /**
     * Is item match for prefix?
     *
     * @param prefix
     * @param upperCasePrefix
     * @param item
     * @return true if match, false otherwise
     */
    protected abstract boolean isMatch(CharSequence prefix,
            String upperCasePrefix, V item);

    @Override
    protected FilterResults performFiltering(CharSequence prefix) {
        FilterResults results = new FilterResults();
        if (TextUtils.isEmpty(prefix))
            return results;

        final List<V> initial = adapter.getInitialItems();
        String upperPrefix = prefix.toString().toUpperCase(US);
        List<V> filtered = new ArrayList<V>();
        for (V item : initial)
            if (isMatch(prefix, upperPrefix, item))
                filtered.add(item);

        results.values = filtered;
        results.count = filtered.size();
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if (results.values != null)
            adapter.setFilteredItems((List<V>) results.values);
        else
            adapter.setItems(adapter.getInitialItems());

        if (results.count == 0)
            adapter.notifyDataSetInvalidated();
    }
}
