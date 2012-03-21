package com.github.mobile.android;

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
     * Image getter
     */
    protected final HttpImageGetter imageGetter;

    /**
     * Create view holder
     *
     * @param imageGetter
     */
    public MarkdownViewHolder(HttpImageGetter imageGetter) {
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
