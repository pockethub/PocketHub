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
package com.github.mobile.repo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.util.AvatarUtils;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.User;

/**
 * View holder to display a user/organization
 */
public class OrgViewHolder implements ViewHolder<User> {

    private final AvatarUtils avatarHelper;

    private final TextView nameText;

    private final ImageView avatarView;

    /**
     * Create org view holder
     *
     * @param view
     * @param avatarHelper
     */
    public OrgViewHolder(final View view, final AvatarUtils avatarHelper) {
        this.avatarHelper = avatarHelper;
        nameText = (TextView) view.findViewById(id.tv_org_name);
        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
    }

    @Override
    public void updateViewFor(final User user) {
        nameText.setText(user.getLogin());
        avatarHelper.bind(avatarView, user);
    }
}
