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
package com.github.mobile.ui.repo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R;
import com.github.mobile.ui.ItemView;

/**
 * Item view for a repository
 */
public class ContributorItemView extends ItemView {

    /**
     * Repository type icon
     */
    public final ImageView avatarView;

    /**
     * Repository name text
     */
    public final TextView loginView;

    /**
     * Repository description text
     */
    public final TextView contributionView;

    /**
     * @param view
     */
    public ContributorItemView(final View view) {
        super(view);

        avatarView = (ImageView) view.findViewById(R.id.iv_avatar);
        loginView = (TextView) view.findViewById(R.id.tv_login);
        contributionView = (TextView) view.findViewById(R.id.tv_contributions);

    }
}