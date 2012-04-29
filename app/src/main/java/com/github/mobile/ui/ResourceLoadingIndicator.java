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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.R.layout;

/**
 * Helper for showing more items are being loaded at the bottom of a list via a custom footer view
 */
public class ResourceLoadingIndicator {

    private final Context context;

    private final View view;

    private final TextView textView;

    private final int loadingResId;

    /**
     * Create indicator using given inflater
     *
     * @param context
     * @param loadingResId
     *            string resource id to show when loading
     */
    public ResourceLoadingIndicator(final Context context, final int loadingResId) {
        this.context = context;
        view = LayoutInflater.from(context).inflate(layout.load_item, null);
        textView = (TextView) view.findViewById(id.tv_loading);
        this.loadingResId = loadingResId;
    }

    /**
     * Set the list view that this indicator should be added as a footer to
     *
     * @param listView
     * @return this indicator
     */
    public ResourceLoadingIndicator setList(final ListView listView) {
        listView.addFooterView(view, null, false);
        return this;
    }

    /**
     * Set visibility of entire indicator view
     *
     * @param visible
     * @return this indicator
     */
    public ResourceLoadingIndicator setVisible(boolean visible) {
        view.setVisibility(visible ? VISIBLE : GONE);
        return this;
    }

    /**
     * Show the indicator as loading state
     *
     * @return this indicator
     */
    public ResourceLoadingIndicator showLoading() {
        setVisible(true);
        textView.setText(context.getString(loadingResId));
        return this;
    }
}
