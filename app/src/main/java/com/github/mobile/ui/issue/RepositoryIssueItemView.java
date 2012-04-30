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
package com.github.mobile.ui.issue;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.TypefaceUtils;

/**
 * Item view for a repository issue
 */
public class RepositoryIssueItemView extends ItemView {

    /**
     * Issue number text
     */
    public final TextView number;

    /**
     * Issue title text
     */
    public final TextView title;

    /**
     * Issue opener avatar
     */
    public final ImageView avatar;

    /**
     * Issue opener login
     */
    public final TextView user;

    /**
     * Issue creation date text
     */
    public final TextView creation;

    /**
     * Number of issue comments
     */
    public final TextView comments;

    /**
     * Icon for pull requests
     */
    public final TextView pullRequestIcon;

    /**
     * Initial paint flags of {@link #number}
     */
    public final int numberPaintFlags;

    /**
     * @param view
     */
    public RepositoryIssueItemView(View view) {
        super(view);

        number = (TextView) view.findViewById(id.tv_issue_number);
        numberPaintFlags = number.getPaintFlags();
        title = (TextView) view.findViewById(id.tv_issue_title);
        avatar = (ImageView) view.findViewById(id.iv_gravatar);
        user = (TextView) view.findViewById(id.tv_issue_user);
        creation = (TextView) view.findViewById(id.tv_issue_creation);
        comments = (TextView) view.findViewById(id.tv_issue_comments);

        pullRequestIcon = (TextView) view.findViewById(id.tv_pull_request_icon);
        TypefaceUtils.setOctocons(pullRequestIcon, (TextView) view.findViewById(id.tv_comment_icon));
    }
}
