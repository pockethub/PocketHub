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
package com.github.mobile.issue;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.util.AvatarUtils;
import com.madgag.android.listviews.ViewHolder;

/**
 * View holder for an {@link IssueFilter}
 */
public class IssueFilterViewHolder implements ViewHolder<IssueFilter> {

    private final AvatarUtils avatarHelper;

    private final ImageView avatarView;

    private final TextView repoText;

    private final TextView filterText;

    /**
     * Create holder for view
     *
     * @param view
     * @param avatarHelper
     */
    public IssueFilterViewHolder(final View view, final AvatarUtils avatarHelper) {
        this.avatarHelper = avatarHelper;
        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
        repoText = (TextView) view.findViewById(id.tv_repo_name);
        filterText = (TextView) view.findViewById(id.tv_filter_summary);
    }

    @Override
    public void updateViewFor(final IssueFilter item) {
        avatarHelper.bind(avatarView, item.getRepository().getOwner());
        repoText.setText(item.getRepository().generateId());
        filterText.setText(item.toDisplay());
    }
}
