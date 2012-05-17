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
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.TypefaceUtils;

/**
 * Item view for a repository
 */
public class RepositoryItemView extends ItemView {

    /**
     * Repository type icon
     */
    public final TextView repoIcon;

    /**
     * Repository name text
     */
    public final TextView repoName;

    /**
     * Repository description text
     */
    public final TextView repoDescription;

    /**
     * Number of watchers
     */
    public final TextView watchers;

    /**
     * Number of forks
     */
    public final TextView forks;

    /**
     * Primary language in repository
     */
    public final TextView language;

    /**
     * @param view
     */
    public RepositoryItemView(final View view) {
        super(view);

        repoIcon = (TextView) view.findViewById(id.tv_repo_icon);
        repoName = (TextView) view.findViewById(id.tv_repo_name);
        repoDescription = (TextView) view.findViewById(id.tv_repo_description);

        TypefaceUtils.setOcticons(repoIcon, (TextView) view.findViewById(id.tv_forks_icon),
                (TextView) view.findViewById(id.tv_watchers_icon));

        language = (TextView) view.findViewById(id.tv_language);
        watchers = (TextView) view.findViewById(id.tv_watchers);
        forks = (TextView) view.findViewById(id.tv_forks);
    }
}
