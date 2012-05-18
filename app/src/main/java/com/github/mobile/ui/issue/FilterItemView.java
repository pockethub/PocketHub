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

/**
 * View of an issue filter
 */
public class FilterItemView extends ItemView {

    /**
     * Avatar of repository owner
     */
    public final ImageView avatarView;

    /**
     * Avatar of assignee
     */
    public final ImageView assigneeAvatarView;

    /**
     * Assignee area
     */
    public final View assigneeArea;

    /**
     * Repository text view
     */
    public final TextView repoText;

    /**
     * Filter state text view
     */
    public final TextView stateText;

    /**
     * Filter labels text view
     */
    public final TextView labelsText;

    /**
     * Filter milestone text view
     */
    public final TextView milestoneText;

    /**
     * Filter assignee text view
     */
    public final TextView assigneeText;

    /**
     * Create holder for view
     *
     * @param view
     */
    public FilterItemView(final View view) {
        super(view);

        avatarView = (ImageView) view.findViewById(id.iv_avatar);
        repoText = (TextView) view.findViewById(id.tv_repo_name);
        stateText = (TextView) view.findViewById(id.tv_filter_state);
        labelsText = (TextView) view.findViewById(id.tv_filter_labels);
        milestoneText = (TextView) view.findViewById(id.tv_filter_milestone);
        assigneeArea = view.findViewById(id.ll_assignee);
        assigneeText = (TextView) view.findViewById(id.tv_filter_assignee);
        assigneeAvatarView = (ImageView)view.findViewById(id.iv_assignee_avatar);
    }
}
