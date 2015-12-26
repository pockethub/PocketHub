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
package com.github.pockethub.ui.ref;

import android.app.Activity;
import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.MultiTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.core.code.FullTree.Entry;
import com.github.pockethub.core.code.FullTree.Folder;
import com.github.pockethub.core.commit.CommitUtils;
import com.github.pockethub.util.ServiceUtils;
import com.github.pockethub.util.TypefaceUtils;

/**
 * Adapter to display a source code tree
 */
public class CodeTreeAdapter extends MultiTypeAdapter {

    private static final int TYPE_BLOB = 0;

    private static final int TYPE_TREE = 1;

    private static final int INDENTED_PADDING = 16;

    private final Context context;

    private final int indentedPaddingLeft;

    private int paddingLeft;

    private int paddingRight;

    private int paddingTop;

    private int paddingBottom;

    private boolean indented;

    /**
     * @param activity
     */
    public CodeTreeAdapter(Activity activity) {
        super(activity);

        this.context = activity;
        indentedPaddingLeft = ServiceUtils.getIntPixels(
                activity.getResources(), INDENTED_PADDING);
    }

    /**
     * Set whether views should be indented
     *
     * @param indented
     * @return this adapter
     */
    public CodeTreeAdapter setIndented(final boolean indented) {
        this.indented = indented;
        return this;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * Set root folder to display
     *
     * @param root
     */
    public void setItems(final Folder root) {
        clear();

        addItems(TYPE_TREE, root.folders.values());
        addItems(TYPE_BLOB, root.files.values());
    }

    @Override
    protected int getChildLayoutId(final int type) {
        switch (type) {
            case TYPE_BLOB:
                return R.layout.blob_item;
            case TYPE_TREE:
                return R.layout.folder_item;
            default:
                return -1;
        }
    }

    @Override
    protected int[] getChildViewIds(final int type) {
        switch (type) {
            case TYPE_BLOB:
                return new int[]{R.id.tv_file, R.id.tv_size};
            case TYPE_TREE:
                return new int[]{R.id.tv_folder, R.id.tv_folders, R.id.tv_files};
            default:
                return null;
        }
    }

    @Override
    protected View initialize(final int type, View view) {
        view = super.initialize(type, view);

        paddingLeft = view.getPaddingLeft();
        paddingRight = view.getPaddingRight();
        paddingTop = view.getPaddingTop();
        paddingBottom = view.getPaddingBottom();

        switch (type) {
            case TYPE_BLOB:
                TypefaceUtils.setOcticons((TextView) view
                        .findViewById(R.id.tv_file_icon));
                break;
            case TYPE_TREE:
                TypefaceUtils.setOcticons(
                        (TextView) view.findViewById(R.id.tv_folder_icon),
                        (TextView) view.findViewById(R.id.tv_folders_icon),
                        (TextView) view.findViewById(R.id.tv_files_icon));
        }

        return view;
    }

    @Override
    protected void update(final int position, final Object item, final int type) {
        if (indented)
            updater.view.setPadding(indentedPaddingLeft, paddingTop,
                    paddingRight, paddingBottom);
        else
            updater.view.setPadding(paddingLeft, paddingTop, paddingRight,
                    paddingBottom);

        switch (type) {
            case TYPE_BLOB:
                Entry file = (Entry) item;
                setText(0, file.name);
                setText(1, Formatter.formatFileSize(context, file.entry.size));
                break;
            case TYPE_TREE:
                Folder folder = (Folder) item;
                setText(0, CommitUtils.getName(folder.name));
                setNumber(1, folder.folders.size());
                setNumber(2, folder.files.size());
                break;
        }
    }
}
