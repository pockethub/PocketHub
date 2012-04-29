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
     * Avatar image view
     */
    public final ImageView avatarView;

    /**
     * Repository text view
     */
    public final TextView repoText;

    /**
     * Filter label text view
     */
    public final TextView filterText;

    /**
     * Create holder for view
     *
     * @param view
     */
    public FilterItemView(final View view) {
        super(view);

        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
        repoText = (TextView) view.findViewById(id.tv_repo_name);
        filterText = (TextView) view.findViewById(id.tv_filter_summary);
    }
}
