package com.github.mobile.android;

import android.content.Context;
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
     * Create view holder
     *
     * @param context
     * @param imageGetter
     */
    public MarkdownViewHolder(Context context, HttpImageGetter imageGetter) {
        this.context = context;
        this.imageGetter = imageGetter;
    }

    /**
     * Bind {@link TextView} to HTML
     *
     * @param textView
     * @param html
     */
    protected void bindHtml(TextView textView, String html) {
        imageGetter.bind(textView, html);
    }
}
