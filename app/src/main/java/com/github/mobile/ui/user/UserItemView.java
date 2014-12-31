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

package com.github.mobile.ui.user;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R;
import com.github.mobile.ui.ItemView;

/**
 * View of a search item
 */
public class UserItemView extends ItemView {

    /**
     * Avatar image view
     */
    public final ImageView avatarView;

    /**
     * Login text view
     */
    public final TextView loginView;

    /**
     * Create news events item view
     *
     * @param view
     */
    public UserItemView(View view) {
        super(view);

        avatarView = (ImageView) view.findViewById(R.id.iv_avatar);
        loginView = (TextView) view.findViewById(R.id.tv_login);
    }
}
