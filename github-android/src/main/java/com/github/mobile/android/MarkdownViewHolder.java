package com.github.mobile.android;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.android.util.HttpImageGetter;
import com.madgag.android.listviews.ViewHolder;

/**
 * View holder for common markdown-rendering views
 *
 * @param <V>
 */
public abstract class MarkdownViewHolder<V> implements ViewHolder<V> {

    /**
     * Context
     */
    protected final Context context;

    /**
     * Image getter
     */
    protected final HttpImageGetter imageGetter;

    /**
     * View
     */
    protected final View view;

    /**
     * Create view holder
     *
     * @param context
     * @param imageGetter
     * @param view
     */
    public MarkdownViewHolder(Context context, HttpImageGetter imageGetter, View view) {
        this.context = context;
        this.imageGetter = imageGetter;
        this.view = view;
    }

    /**
     * Bind {@link TextView} at id to HTML
     *
     * @param id
     * @param html
     */
    protected void bindHtml(int id, String html) {
        imageGetter.bind((TextView) view.findViewById(id), html);
    }
}
