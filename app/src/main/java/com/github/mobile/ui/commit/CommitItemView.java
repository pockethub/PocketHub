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

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R;
import com.github.mobile.ui.ItemView;

/**
 * Item view for a comment
 */
public class CommitItemView extends ItemView {

    /**
     * Comment author text
     */
    public final TextView idView;

    /**
     * Comment author text
     */
    public final TextView authorView;

    /**
     * Comment author avatar image view
     */
    public final ImageView avatarView;

    /**
     * Comment author text
     */
    public final TextView messageView;

    /**
     * Comment author text
     */
    public final TextView commentView;

    /**
     * @param view
     */
    public CommitItemView(final View view) {
        super(view);

        idView = (TextView) view.findViewById(R.id.tv_commit_id);
        authorView = (TextView) view.findViewById(R.id.tv_commit_author);
        avatarView = (ImageView) view.findViewById(R.id.iv_avatar);
        messageView = (TextView) view.findViewById(R.id.tv_commit_message);
        commentView = (TextView) view.findViewById(R.id.tv_commit_comments);
    }
}