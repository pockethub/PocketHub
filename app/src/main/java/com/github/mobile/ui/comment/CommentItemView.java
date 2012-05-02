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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.TypefaceUtils;

/**
 * Item view for a comment
 */
public class CommentItemView extends ItemView {

    /**
     * Comment author text
     */
    public final TextView authorView;

    /**
     * Comment date text
     */
    public final TextView dateView;

    /**
     * Comment body text
     */
    public final TextView bodyView;

    /**
     * Comment author avatar image view
     */
    public final ImageView avatarView;

    /**
     * @param view
     */
    public CommentItemView(final View view) {
        super(view);

        bodyView = (TextView) view.findViewById(id.tv_comment_body);
        bodyView.setMovementMethod(LinkMovementMethod.getInstance());

        authorView = (TextView) view.findViewById(id.tv_comment_author);
        dateView = (TextView) view.findViewById(id.tv_comment_date);
        avatarView = (ImageView) view.findViewById(id.iv_avatar);

        TypefaceUtils.setOcticons((TextView) view.findViewById(id.tv_comment_icon));
    }
}
