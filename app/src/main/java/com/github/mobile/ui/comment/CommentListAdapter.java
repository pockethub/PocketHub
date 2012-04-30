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

import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.HttpImageGetter;
import com.github.mobile.util.TimeUtils;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.Comment;

/**
 * Adapter for a list of {@link Comment} objects
 */
public class CommentListAdapter extends ItemListAdapter<Comment, CommentItemView> {

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
    public CommentListAdapter(LayoutInflater inflater, Comment[] elements, AvatarLoader avatars,
            HttpImageGetter imageGetter) {
        super(layout.comment_view_item, inflater, elements);

        this.avatars = avatars;
        this.imageGetter = imageGetter;
    }

    /**
     * Create list adapter
     *
     * @param inflater
     * @param avatars
     * @param imageGetter
     */
    public CommentListAdapter(LayoutInflater inflater, AvatarLoader avatars, HttpImageGetter imageGetter) {
        this(inflater, null, avatars, imageGetter);
    }

    @Override
    protected void update(final CommentItemView view, final Comment comment) {
        imageGetter.bind(view.bodyView, comment.getBodyHtml(), comment.getId());
        avatars.bind(view.avatarView, comment.getUser());

        view.authorView.setText(comment.getUser().getLogin());
        view.dateView.setText(TimeUtils.getRelativeTime(comment.getUpdatedAt()));
    }

    @Override
    protected CommentItemView createView(final View view) {
        return new CommentItemView(view);
    }
}
