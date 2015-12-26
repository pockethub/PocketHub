/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.ui.repo;

import android.text.TextUtils;
import android.view.LayoutInflater;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.ViewUtils;

import static com.github.pockethub.util.TypefaceUtils.ICON_FORK;
import static com.github.pockethub.util.TypefaceUtils.ICON_MIRROR_PRIVATE;
import static com.github.pockethub.util.TypefaceUtils.ICON_MIRROR_PUBLIC;
import static com.github.pockethub.util.TypefaceUtils.ICON_PRIVATE;
import static com.github.pockethub.util.TypefaceUtils.ICON_PUBLIC;

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
                setText(0, ICON_PRIVATE);
            else if (isFork)
                setText(0, ICON_FORK);
            else
                setText(0, ICON_PUBLIC);
        else {
            if (isPrivate)
                setText(0, ICON_MIRROR_PRIVATE);
            else
                setText(0, ICON_MIRROR_PUBLIC);
        }

        if (!TextUtils.isEmpty(description))
            ViewUtils.setGone(setText(1, description), false);
        else
            setGone(1, true);

        if (!TextUtils.isEmpty(language))
            ViewUtils.setGone(setText(2, language), false);
        else
            setGone(2, true);

        setNumber(3, watchers);
        setNumber(4, forks);
    }
}
