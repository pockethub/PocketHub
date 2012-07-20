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

import com.github.mobile.ui.ItemView;
import com.viewpagerindicator.R.id;

/**
 * Item view for a commit
 */
public class CommitView extends ItemView {

    /**
     * Commit id
     */
    public final TextView sha;

    /**
     * Commit author
     */
    public final TextView author;

    /**
     * Commit author avatar
     */
    public final ImageView avatar;

    /**
     * Commit message
     */
    public final TextView message;

    /**
     * Create view
     *
     * @param view
     */
    public CommitView(final View view) {
        super(view);

        sha = textView(view, id.tv_commit_id);
        author = textView(view, id.tv_commit_author);
        avatar = imageView(view, id.iv_avatar);
        message = textView(view, id.tv_commit_message);
    }
}
