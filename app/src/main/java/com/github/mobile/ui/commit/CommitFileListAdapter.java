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

import com.github.kevinsawicki.wishlist.MultiTypeAdapter;
import com.github.mobile.R.layout;
import com.github.mobile.core.commit.FullCommitFile;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.HttpImageGetter;
import com.github.mobile.util.TimeUtils;
import com.github.mobile.util.ViewUtils;
import com.viewpagerindicator.R.id;

import java.util.List;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.CommitFile;

/**
 * Adapter to display a list of files changed in commits
 */
public class CommitFileListAdapter extends MultiTypeAdapter {

    private static final int TYPE_FILE_HEADER = 0;

    private static final int TYPE_FILE_LINE = 1;

    private static final int TYPE_LINE_COMMENT = 2;

    private static final int TYPE_COMMENT = 3;

    private final DiffStyler diffStyler;

    private final HttpImageGetter imageGetter;

    private final AvatarLoader avatars;

    /**
     * @param inflater
     * @param diffStyler
     * @param avatars
     * @param imageGetter
     */
    public CommitFileListAdapter(LayoutInflater inflater,
            DiffStyler diffStyler, AvatarLoader avatars,
            HttpImageGetter imageGetter) {
        super(inflater);

        this.diffStyler = diffStyler;
        this.avatars = avatars;
        this.imageGetter = imageGetter;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    /**
     * Add file to adapter
     *
     * @param file
     */
    public void addItem(FullCommitFile file) {
        addItem(TYPE_FILE_HEADER, file.getFile());
        List<CharSequence> lines = diffStyler.get(file.getFile().getFilename());
        int number = 0;
        for (CharSequence line : lines) {
            addItem(TYPE_FILE_LINE, line);
            for (CommitComment comment : file.get(number))
                addItem(TYPE_LINE_COMMENT, comment);
            number++;
        }
    }

    /**
     * Add file to adapter
     *
     * @param file
     */
    public void addItem(CommitFile file) {
        addItem(TYPE_FILE_HEADER, file);
        addItems(TYPE_FILE_LINE, diffStyler.get(file.getFilename()));
    }

    /**
     * Add comment to adapter
     *
     * @param comment
     */
    public void addComment(CommitComment comment) {
        addItem(TYPE_COMMENT, comment);
    }

    protected int getChildLayoutId(int type) {
        switch (type) {
        case TYPE_FILE_HEADER:
            return layout.commit_diff_file_header;
        case TYPE_FILE_LINE:
            return layout.commit_diff_line;
        case TYPE_LINE_COMMENT:
            return layout.diff_comment_item;
        case TYPE_COMMENT:
            return layout.commit_comment_item;
        default:
            return -1;
        }
    }

    @Override
    protected int[] getChildViewIds(int type) {
        switch (type) {
        case TYPE_FILE_HEADER:
            return new int[] { id.tv_name, id.tv_folder };
        case TYPE_FILE_LINE:
            return new int[] { id.tv_diff };
        case TYPE_LINE_COMMENT:
        case TYPE_COMMENT:
            return new int[] { id.tv_comment_body, id.iv_avatar,
                    id.tv_comment_author, id.tv_comment_date };
        default:
            return null;
        }
    }

    @Override
    protected void update(int position, Object item, int type) {
        switch (type) {
        case TYPE_FILE_HEADER:
            String path = ((CommitFile) item).getFilename();
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash != -1) {
                setText(id.tv_name, path.substring(lastSlash + 1));
                ViewUtils
                        .setGone(
                                setText(id.tv_folder,
                                        path.substring(0, lastSlash + 1)),
                                false);
            } else {
                setText(id.tv_name, path);
                setGone(id.tv_folder, true);
            }
            return;
        case TYPE_FILE_LINE:
            CharSequence text = (CharSequence) item;
            diffStyler.updateColors((CharSequence) item,
                    setText(id.tv_diff, text));
            return;
        case TYPE_LINE_COMMENT:
        case TYPE_COMMENT:
            CommitComment comment = (CommitComment) item;
            avatars.bind(imageView(id.iv_avatar), comment.getUser());
            setText(id.tv_comment_author, comment.getUser().getLogin());
            setText(id.tv_comment_date,
                    TimeUtils.getRelativeTime(comment.getUpdatedAt()));
            imageGetter.bind(textView(id.tv_comment_body),
                    comment.getBodyHtml(), comment.getId());
            return;
        default:
            return;
        }
    }
}
