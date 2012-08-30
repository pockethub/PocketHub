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
package com.github.mobile.ui.comment;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.HttpImageGetter;
import com.github.mobile.util.TimeUtils;

import org.eclipse.egit.github.core.Comment;

/**
 * Adapter for a list of {@link Comment} objects
 */
public class CommentListAdapter extends SingleTypeAdapter<Comment> {

    private final AvatarLoader avatars;

    private final HttpImageGetter imageGetter;

    /**
     * Create list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     * @param imageGetter
     */
    public CommentListAdapter(LayoutInflater inflater, Comment[] elements,
            AvatarLoader avatars, HttpImageGetter imageGetter) {
        super(inflater, layout.comment_item);

        this.avatars = avatars;
        this.imageGetter = imageGetter;
        setItems(elements);
    }

    /**
     * Create list adapter
     *
     * @param inflater
     * @param avatars
     * @param imageGetter
     */
    public CommentListAdapter(LayoutInflater inflater, AvatarLoader avatars,
            HttpImageGetter imageGetter) {
        this(inflater, null, avatars, imageGetter);
    }

    @Override
    protected void update(int position, Comment comment) {
        imageGetter.bind(textView(id.tv_comment_body), comment.getBodyHtml(),
                comment.getId());
        avatars.bind(imageView(id.iv_avatar), comment.getUser());

        setText(id.tv_comment_author, comment.getUser().getLogin());
        setText(id.tv_comment_date,
                TimeUtils.getRelativeTime(comment.getUpdatedAt()));
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).getId();
    }

    protected View initialize(View view) {
        view = super.initialize(view);

        textView(view, id.tv_comment_body).setMovementMethod(
                LinkMovementMethod.getInstance());
        return view;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { id.tv_comment_body, id.tv_comment_author,
                id.tv_comment_date, id.iv_avatar };
    }
}
