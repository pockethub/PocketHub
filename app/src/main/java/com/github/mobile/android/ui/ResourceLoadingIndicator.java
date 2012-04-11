package com.github.mobile.android.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;

/**
 * Helper for showing more items are being loaded at the bottom of a list via a custom footer view
 */
public class ResourceLoadingIndicator {

    private final Context context;

    private final View view;

    private final TextView textView;

    private final ProgressBar progressBar;

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
        progressBar = (ProgressBar) view.findViewById(id.pb_loading);
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
        progressBar.setVisibility(VISIBLE);
        textView.setText(context.getString(loadingResId));
        return this;
    }
}
