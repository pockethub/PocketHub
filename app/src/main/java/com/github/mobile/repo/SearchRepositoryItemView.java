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

import com.github.mobile.R.id;

import android.view.View;
import android.widget.TextView;

/**
 * Item view for a searched for repository
 */
public class SearchRepositoryItemView extends RepositoryItemView {

    /**
     * Repository description text view
     */
    public final TextView repoDescription;

    /**
     * @param view
     */
    public SearchRepositoryItemView(final View view) {
        super(view);

        repoDescription = (TextView) view.findViewById(id.tv_repo_description);
    }
}
