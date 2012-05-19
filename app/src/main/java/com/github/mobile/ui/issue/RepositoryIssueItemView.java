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
 * View used to display a repository issue
 */
public class RepositoryIssueItemView extends ItemView {

    /**
     * Issue number text view
     */
    public final TextView number;

    /**
     * Issue title text view
     */
    public final TextView title;

    /**
     * Issue opener avatar image view
     */
    public final ImageView avatar;

    /**
     * Issue reporter text view
     */
    public final TextView reporter;

    /**
     * Number of comments text view
     */
    public final TextView comments;

    /**
     * Pull request icon text view
     */
    public final TextView pullRequestIcon;

    /**
     * Label stripes
     */
    public final View[] labels;

    /**
     * Initial paint flags of {@link #number}
     */
    public final int numberPaintFlags;

    /**
     * Create item view
     *
     * @param view
     */
    public RepositoryIssueItemView(View view) {
        super(view);

        number = (TextView) view.findViewById(id.tv_issue_number);
        numberPaintFlags = number.getPaintFlags();
        title = (TextView) view.findViewById(id.tv_issue_title);
        avatar = (ImageView) view.findViewById(id.iv_avatar);
        reporter = (TextView) view.findViewById(id.tv_issue_creation);
        comments = (TextView) view.findViewById(id.tv_issue_comments);

        pullRequestIcon = (TextView) view.findViewById(id.tv_pull_request_icon);
        TypefaceUtils.setOcticons(pullRequestIcon, (TextView) view.findViewById(id.tv_comment_icon));

        labels = new View[8];
        labels[0] = view.findViewById(id.v_label0);
        labels[1] = view.findViewById(id.v_label1);
        labels[2] = view.findViewById(id.v_label2);
        labels[3] = view.findViewById(id.v_label3);
        labels[4] = view.findViewById(id.v_label4);
        labels[5] = view.findViewById(id.v_label5);
        labels[6] = view.findViewById(id.v_label6);
        labels[7] = view.findViewById(id.v_label7);
    }
}
