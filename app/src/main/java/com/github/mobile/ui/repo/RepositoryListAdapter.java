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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.util.TypefaceUtils.ICON_FORK;
import static com.github.mobile.util.TypefaceUtils.ICON_PRIVATE;
import static com.github.mobile.util.TypefaceUtils.ICON_PUBLIC;
import android.text.TextUtils;
import android.view.LayoutInflater;

import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;

import java.text.NumberFormat;

/**
 * Adapter for a list of repositories
 *
 * @param <I>
 *            item class
 * @param <V>
 *            view class
 */
public abstract class RepositoryListAdapter<I, V extends ItemView> extends ItemListAdapter<I, V> {

    /**
     * Number formatter
     */
    protected static final NumberFormat FORMAT = NumberFormat.getIntegerInstance();

    /**
     * Create list adapter
     *
     * @param viewId
     * @param inflater
     * @param elements
     */
    public RepositoryListAdapter(int viewId, LayoutInflater inflater, I[] elements) {
        super(viewId, inflater, elements);
    }

    /**
     * Create list adapter
     *
     * @param viewId
     * @param inflater
     */
    public RepositoryListAdapter(int viewId, LayoutInflater inflater) {
        super(viewId, inflater);
    }

    /**
     * Update repository details
     *
     * @param view
     * @param description
     * @param language
     * @param watchers
     * @param forks
     * @param isPrivate
     * @param isFork
     */
    protected void updateDetails(final RepositoryItemView view, final String description, final String language,
            final int watchers, final int forks, final boolean isPrivate, final boolean isFork) {
        if (isPrivate)
            view.repoIcon.setText(Character.toString(ICON_PRIVATE));
        else if (isFork)
            view.repoIcon.setText(Character.toString(ICON_FORK));
        else
            view.repoIcon.setText(Character.toString(ICON_PUBLIC));

        if (!TextUtils.isEmpty(description)) {
            view.repoDescription.setText(description);
            view.repoDescription.setVisibility(VISIBLE);
        } else
            view.repoDescription.setVisibility(GONE);

        if (TextUtils.isEmpty(language))
            view.language.setVisibility(GONE);
        else {
            view.language.setText(language);
            view.language.setVisibility(VISIBLE);
        }

        view.watchers.setText(FORMAT.format(watchers));
        view.forks.setText(FORMAT.format(forks));
    }
}
