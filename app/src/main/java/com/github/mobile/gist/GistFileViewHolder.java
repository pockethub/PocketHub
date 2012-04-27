package com.github.mobile.gist;

import android.view.View;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.util.TypefaceHelper;
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
        nameText = (TextView) view.findViewById(id.tv_file);
        ((TextView) view.findViewById(id.tv_file_icon)).setTypeface(TypefaceHelper.getOctocons(view.getContext()));
    }

    public void updateViewFor(final GistFile file) {
        nameText.setText(file.getFilename());
    }
}
