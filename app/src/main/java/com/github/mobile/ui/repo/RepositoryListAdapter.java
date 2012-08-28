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

import static com.github.mobile.util.TypefaceUtils.ICON_FORK;
import static com.github.mobile.util.TypefaceUtils.ICON_MIRROR_PRIVATE;
import static com.github.mobile.util.TypefaceUtils.ICON_MIRROR_PUBLIC;
import static com.github.mobile.util.TypefaceUtils.ICON_PRIVATE;
import static com.github.mobile.util.TypefaceUtils.ICON_PUBLIC;
import android.text.TextUtils;
import android.view.LayoutInflater;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.viewpagerindicator.R.id;

/**
 * Adapter for a list of repositories
 *
 * @param <V>
 */
public abstract class RepositoryListAdapter<V> extends SingleTypeAdapter<V> {

    /**
     * Create list adapter
     *
     * @param viewId
     * @param inflater
     * @param elements
     */
    public RepositoryListAdapter(int viewId, LayoutInflater inflater,
            Object[] elements) {
        super(inflater, viewId);

        setItems(elements);
    }

    /**
     * Create list adapter
     *
     * @param viewId
     * @param inflater
     */
    public RepositoryListAdapter(int viewId, LayoutInflater inflater) {
        super(inflater, viewId);
    }

    /**
     * Update repository details
     *
     * @param description
     * @param language
     * @param watchers
     * @param forks
     * @param isPrivate
     * @param isFork
     * @param mirrorUrl
     */
    protected void updateDetails(final String description,
            final String language, final int watchers, final int forks,
            final boolean isPrivate, final boolean isFork,
            final String mirrorUrl) {
        if (TextUtils.isEmpty(mirrorUrl))
            if (isPrivate)
                setText(id.tv_repo_icon, ICON_PRIVATE);
            else if (isFork)
                setText(id.tv_repo_icon, ICON_FORK);
            else
                setText(id.tv_repo_icon, ICON_PUBLIC);
        else {
            if (isPrivate)
                setText(id.tv_repo_icon, ICON_MIRROR_PRIVATE);
            else
                setText(id.tv_repo_icon, ICON_MIRROR_PUBLIC);
        }

        if (!TextUtils.isEmpty(description))
            ViewUtils.setGone(setText(id.tv_repo_description, description),
                    false);
        else
            setGone(id.tv_repo_description, true);

        if (!TextUtils.isEmpty(language))
            ViewUtils.setGone(setText(id.tv_language, language), false);
        else
            setGone(id.tv_language, true);

        setNumber(id.tv_watchers, watchers);
        setNumber(id.tv_forks, forks);
    }
}
