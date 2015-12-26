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
package com.github.pockethub.ui.commit;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;

import com.alorma.github.sdk.bean.dto.response.CommitComment;
import com.alorma.github.sdk.bean.dto.response.CommitFile;
import com.github.kevinsawicki.wishlist.MultiTypeAdapter;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.R;
import com.github.pockethub.core.commit.FullCommitFile;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.HttpImageGetter;
import com.github.pockethub.util.TimeUtils;

import java.util.List;

import static com.github.kevinsawicki.wishlist.ViewUpdater.FORMAT_INT;


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

    private final int addTextColor;

    private final int removeTextColor;

    /**
     * @param inflater
     * @param diffStyler
     * @param avatars
     * @param imageGetter
     */
    public CommitFileListAdapter(final LayoutInflater inflater,
            final DiffStyler diffStyler, final AvatarLoader avatars,
            final HttpImageGetter imageGetter) {
        super(inflater);

        this.diffStyler = diffStyler;
        this.avatars = avatars;
        this.imageGetter = imageGetter;

        Resources resources = inflater.getContext().getResources();
        addTextColor = resources.getColor(R.color.diff_add_text);
        removeTextColor = resources.getColor(R.color.diff_remove_text);
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public long getItemId(int position) {
        switch (getItemViewType(position)) {
        case TYPE_FILE_HEADER:
            String sha = ((CommitFile) getItem(position)).sha;
            if (!TextUtils.isEmpty(sha))
                return sha.hashCode();
            else
                return super.getItemId(position);
        case TYPE_COMMENT:
        case TYPE_LINE_COMMENT:
            return Long.parseLong(((CommitComment) getItem(position)).id);
        default:
            return super.getItemId(position);
        }

    }

    /**
     * Add file to adapter
     *
     * @param file
     */
    public void addItem(final FullCommitFile file) {
        addItem(TYPE_FILE_HEADER, file.getFile());
        List<CharSequence> lines = diffStyler.get(file.getFile().filename);
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
    public void addItem(final CommitFile file) {
        addItem(TYPE_FILE_HEADER, file);
        addItems(TYPE_FILE_LINE, diffStyler.get(file.filename));
    }

    /**
     * Add comment to adapter
     *
     * @param comment
     */
    public void addComment(final CommitComment comment) {
        addItem(TYPE_COMMENT, comment);
    }

    @Override
    protected int getChildLayoutId(final int type) {
        switch (type) {
        case TYPE_FILE_HEADER:
            return R.layout.commit_diff_file_header;
        case TYPE_FILE_LINE:
            return R.layout.commit_diff_line;
        case TYPE_LINE_COMMENT:
            return R.layout.diff_comment_item;
        case TYPE_COMMENT:
            return R.layout.commit_comment_item;
        default:
            return -1;
        }
    }

    @Override
    protected int[] getChildViewIds(final int type) {
        switch (type) {
        case TYPE_FILE_HEADER:
            return new int[] { R.id.tv_name, R.id.tv_folder, R.id.tv_stats };
        case TYPE_FILE_LINE:
            return new int[] { R.id.tv_diff };
        case TYPE_LINE_COMMENT:
        case TYPE_COMMENT:
            return new int[] { R.id.tv_comment_body, R.id.iv_avatar,
                    R.id.tv_comment_author, R.id.tv_comment_date };
        default:
            return null;
        }
    }

    @Override
    protected void update(final int position, final Object item, final int type) {
        switch (type) {
        case TYPE_FILE_HEADER:
            CommitFile file = (CommitFile) item;
            String path = file.filename;
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash != -1) {
                setText(0, path.substring(lastSlash + 1));
                ViewUtils.setGone(setText(1, path.substring(0, lastSlash + 1)),
                        false);
            } else {
                setText(0, path);
                setGone(1, true);
            }

            StyledText stats = new StyledText();
            stats.foreground('+', addTextColor);
            stats.foreground(FORMAT_INT.format(file.additions),
                    addTextColor);
            stats.append(' ').append(' ').append(' ');
            stats.foreground('-', removeTextColor);
            stats.foreground(FORMAT_INT.format(file.deletions),
                    removeTextColor);
            setText(2, stats);
            return;
        case TYPE_FILE_LINE:
            CharSequence text = (CharSequence) item;
            diffStyler.updateColors((CharSequence) item, setText(0, text));
            return;
        case TYPE_LINE_COMMENT:
        case TYPE_COMMENT:
            CommitComment comment = (CommitComment) item;
            avatars.bind(imageView(1), comment.user);
            setText(2, comment.user.login);
            setText(3, TimeUtils.getRelativeTime(comment.updated_at));
            imageGetter.bind(textView(0), comment.body_html,
                    comment.id);
        }
    }
}
