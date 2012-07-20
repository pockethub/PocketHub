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
package com.github.mobile.ui.commit;

import android.view.View;
import android.widget.TextView;

import com.github.mobile.ui.ItemView;
import com.viewpagerindicator.R.id;

/**
 *
 */
public class CommitFileView extends ItemView {

    /**
     * File name text
     */
    public final TextView name;

    /**
     * Directory text
     */
    public final TextView folder;

    /**
     * Diff text
     */
    public TextView diff;

    /**
     * @param view
     */
    public CommitFileView(View view) {
        super(view);

        name = (TextView) view.findViewById(id.tv_name);
        diff = (TextView) view.findViewById(id.tv_diff);
        folder = (TextView) view.findViewById(id.tv_folder);
    }
}
