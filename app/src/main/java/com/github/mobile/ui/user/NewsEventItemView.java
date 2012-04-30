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

import com.github.mobile.R.id;
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.TypefaceUtils;

/**
 * View of a news item
 */
public class NewsEventItemView extends ItemView {

    /**
     * Avatar image view
     */
    public final ImageView avatarView;

    /**
     * Event text view
     */
    public final TextView eventText;

    /**
     * Event icon view
     */
    public final TextView iconText;

    /**
     * Event date text view
     */
    public final TextView dateText;

    /**
     * Create news events item view
     *
     * @param view
     */
    public NewsEventItemView(View view) {
        super(view);

        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
        eventText = (TextView) view.findViewById(id.tv_event);

        iconText = (TextView) view.findViewById(id.tv_event_icon);
        TypefaceUtils.setOctocons(iconText);

        dateText = (TextView) view.findViewById(id.tv_event_date);
    }
}
