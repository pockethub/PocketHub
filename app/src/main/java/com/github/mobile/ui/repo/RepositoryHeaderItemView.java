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
import android.widget.TextView;

import com.github.mobile.R.id;

/**
 * Repository item view with an optional header
 */
public class RepositoryHeaderItemView extends RepositoryItemView {

    /**
     * Header area
     */
    public final View header;

    /**
     * Header text view
     */
    public final TextView headerText;

    /**
     * Separator
     */
    public final View separator;

    /**
     * Create item view
     *
     * @param view
     */
    public RepositoryHeaderItemView(final View view) {
        super(view);

        header = view.findViewById(id.ll_header);
        headerText = (TextView) header.findViewById(id.tv_header);
        separator = view.findViewById(id.v_separator);
    }
}
