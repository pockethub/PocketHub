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

import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.R.layout;
import com.github.mobile.core.issue.IssueFilter;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.util.AvatarLoader;

/**
 * Adapter to display a list of {@link IssueFilter} objects
 */
public class FilterListAdapter extends ItemListAdapter<IssueFilter, FilterItemView> {

    private final AvatarLoader avatars;

    /**
     * Create {@link IssueFilter} list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     */
    public FilterListAdapter(LayoutInflater inflater, IssueFilter[] elements, AvatarLoader avatars) {
        super(layout.issue_filter_item, inflater, elements);

        this.avatars = avatars;
    }

    /**
     * Create {@link IssueFilter} list adapter
     *
     * @param inflater
     * @param avatars
     */
    public FilterListAdapter(LayoutInflater inflater, AvatarLoader avatars) {
        this(inflater, null, avatars);
    }

    @Override
    protected void update(final FilterItemView view, final IssueFilter filter) {
        avatars.bind(view.avatarView, filter.getRepository().getOwner());
        view.repoText.setText(filter.getRepository().generateId());
        view.filterText.setText(filter.toDisplay());
    }

    @Override
    protected FilterItemView createView(final View view) {
        return new FilterItemView(view);
    }
}
