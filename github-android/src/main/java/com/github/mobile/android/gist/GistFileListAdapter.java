package com.github.mobile.android.gist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.github.mobile.android.R;
import com.github.mobile.android.util.SourceEditor;

import org.eclipse.egit.github.core.GistFile;

/**
 * Adapter for viewing the files in a Gist
 */
public class GistFileListAdapter extends BaseExpandableListAdapter {

    private final GistFile[] files;

    private final LayoutInflater inflater;

    /**
     * Create adapter for files
     *
     * @param files
     * @param inflater
     */
    public GistFileListAdapter(GistFile[] files, LayoutInflater inflater) {
        this.files = files;
        this.inflater = inflater;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public boolean hasStableIds() {
        return true;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView view = (TextView) inflater.inflate(R.layout.gist_view_file_item, null);
        view.setText(files[groupPosition].getFilename());
        return view;
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public int getGroupCount() {
        return files.length;
    }

    public Object getGroup(int groupPosition) {
        return files[groupPosition];
    }

    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent) {
        WebView view = (WebView) inflater.inflate(R.layout.gist_view_content_item, null);
        final GistFile file = files[groupPosition];
        return SourceEditor.showSource(view, new Object() {

            public String toString() {
                return file.getContent();
            }
        });
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public Object getChild(int groupPosition, int childPosition) {
        return files[groupPosition];
    }
}
