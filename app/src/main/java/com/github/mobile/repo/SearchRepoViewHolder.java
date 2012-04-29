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
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.util.TypefaceUtils;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.SearchRepository;

/**
 * View holder for a search repository displayed in a list
 */
public class SearchRepoViewHolder implements ViewHolder<SearchRepository> {

    private final TextView repoIcon;

    private final TextView repoDescription;

    private final TextView repoName;

    /**
     * Create search repository view holder
     *
     * @param view
     */
    public SearchRepoViewHolder(final View view) {
        repoIcon = (TextView) view.findViewById(id.tv_repo_icon);
        TypefaceUtils.setOctocons(repoIcon);
        repoName = (TextView) view.findViewById(id.tv_repo_name);
        repoDescription = (TextView) view.findViewById(id.tv_repo_description);
    }

    @Override
    public void updateViewFor(final SearchRepository repo) {
        if (repo.isFork())
            repoIcon.setText("\uf202");
        else
            repoIcon.setText("\uf201");

        repoName.setText(repo.generateId());
        repoDescription.setText(repo.getDescription());
    }
}
