package com.github.mobile.android.gist;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.mobile.android.R.layout;

import org.eclipse.egit.github.core.GistFile;

/**
 * Adapter for viewing the files in a Gist
 */
public class GistFileListAdapter extends ArrayAdapter<GistFile> {

    private final Activity activity;

    /**
     * Create adapter for files
     *
     * @param activity
     * @param files
     */
    public GistFileListAdapter(Activity activity, GistFile[] files) {
        super(activity, layout.gist_view_file_item, files);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) activity.getLayoutInflater().inflate(layout.gist_view_file_item, null);
        view.setText("»   " + getItem(position).getFilename());
        return view;
    }
}
