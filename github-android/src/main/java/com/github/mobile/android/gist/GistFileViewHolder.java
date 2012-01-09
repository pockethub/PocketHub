package com.github.mobile.android.gist;

import android.view.View;
import android.widget.TextView;

import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.GistFile;

/**
 * Holder for a file associated with a Gist
 */
public class GistFileViewHolder implements ViewHolder<GistFile> {

    private final TextView nameText;

    /**
     * Create holder for view
     *
     * @param view
     */
    public GistFileViewHolder(final View view) {
        nameText = (TextView) view;
    }

    public void updateViewFor(final GistFile file) {
        nameText.setText("»   " + file.getFilename());
    }
}
