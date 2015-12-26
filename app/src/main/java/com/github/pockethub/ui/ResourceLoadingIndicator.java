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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.R;

/**
 * Helper for showing more items are being loaded at the bottom of a list via a
 * custom footer view
 */
public class ResourceLoadingIndicator {

    private HeaderFooterListAdapter<?> adapter;

    private boolean showing;

    private final View view;

    private final TextView textView;

    /**
     * Create indicator using given inflater
     *
     * @param context
     * @param loadingResId
     *            string resource id to show when loading
     */
    public ResourceLoadingIndicator(final Context context,
            final int loadingResId) {
        view = LayoutInflater.from(context).inflate(R.layout.loading_item, null);
        textView = (TextView) view.findViewById(R.id.tv_loading);
        textView.setText(loadingResId);
    }

    /**
     * Set the adapter that this indicator should be added as a footer to
     *
     * @param adapter
     * @return this indicator
     */
    public ResourceLoadingIndicator setList(
            final HeaderFooterListAdapter<?> adapter) {
        this.adapter = adapter;
        adapter.addFooter(view);
        showing = true;
        return this;
    }

    /**
     * Set visibility of entire indicator view
     *
     * @param visible
     * @return this indicator
     */
    public ResourceLoadingIndicator setVisible(final boolean visible) {
        if (showing != visible && adapter != null)
            if (visible)
                adapter.addFooter(view);
            else
                adapter.removeFooter(view);
        showing = visible;
        return this;
    }
}
