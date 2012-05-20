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
package com.github.mobile.ui.gist;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.TypefaceUtils;

import org.eclipse.egit.github.core.Gist;

/**
 * View of a {@link Gist} in a list
 */
public class GistView extends ItemView {

    /**
     * Gist id text view
     */
    public final TextView gistId;

    /**
     * Gist title text view
     */
    public final TextView title;

    /**
     * Gist author text view
     */
    public final TextView author;

    /**
     * Number of comments text view
     */
    public final TextView comments;

    /**
     * Number of files text view
     */
    public final TextView files;

    /**
     * Avatar image view
     */
    public final ImageView avatar;

    /**
     * Create view of a {@link Gist} in a list
     *
     * @param view
     */
    public GistView(View view) {
        super(view);

        gistId = (TextView) view.findViewById(id.tv_gist_id);
        title = (TextView) view.findViewById(id.tv_gist_title);
        author = (TextView) view.findViewById(id.tv_gist_author);
        comments = (TextView) view.findViewById(id.tv_gist_comments);
        files = (TextView) view.findViewById(id.tv_gist_files);
        avatar = (ImageView) view.findViewById(id.iv_avatar);

        TypefaceUtils.setOcticons((TextView) view.findViewById(id.tv_comment_icon),
                (TextView) view.findViewById(id.tv_file_icon));
    }
}
