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
package com.github.mobile.ui.commit;

import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.util.ViewUtils;

import org.eclipse.egit.github.core.CommitFile;

/**
 * Adapter to display a list of files changed in commits
 */
public class CommitFileListAdapter extends
        ItemListAdapter<CommitFile, CommitFileView> {

    private DiffStyler diffStyler;

    /**
     * @param viewId
     * @param inflater
     * @param diffStyler
     */
    public CommitFileListAdapter(int viewId, LayoutInflater inflater,
            DiffStyler diffStyler) {
        super(viewId, inflater);

        this.diffStyler = diffStyler;
    }

    @Override
    protected void update(int position, CommitFileView view, CommitFile item) {
        String path = item.getFilename();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            view.name.setText(path.substring(lastSlash + 1));
            view.folder.setText(path.substring(0, lastSlash));
            ViewUtils.setGone(view.folder, false);
        } else {
            view.name.setText(path);
            ViewUtils.setGone(view.folder, true);
        }

        view.diff.setText(diffStyler.get(item.getFilename()));
    }

    @Override
    protected CommitFileView createView(final View view) {
        return new CommitFileView(view);
    }
}
