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

import static com.github.mobile.util.TypefaceUtils.ICON_FORK;
import static com.github.mobile.util.TypefaceUtils.ICON_PRIVATE;
import static com.github.mobile.util.TypefaceUtils.ICON_PUBLIC;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.ui.ItemListAdapter;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.SearchRepository;

/**
 * Adapter for a list of searched for repositories
 */
public class SearchRepositoryListAdapter extends ItemListAdapter<SearchRepository, SearchRepositoryItemView> {

    /**
     * Create list adapter for searched for repositories
     *
     * @param inflater
     * @param elements
     */
    public SearchRepositoryListAdapter(LayoutInflater inflater, SearchRepository[] elements) {
        super(layout.repo_search_list_item, inflater, elements);
    }

    /**
     *
     * Create list adapter for searched for repositories
     *
     * @param inflater
     */
    public SearchRepositoryListAdapter(LayoutInflater inflater) {
        this(inflater, null);
    }

    @Override
    protected void update(final SearchRepositoryItemView view, final SearchRepository repository) {
        if (repository.isPrivate())
            view.repoIcon.setText(Character.toString(ICON_PRIVATE));
        else if (repository.isFork())
            view.repoIcon.setText(Character.toString(ICON_FORK));
        else
            view.repoIcon.setText(Character.toString(ICON_PUBLIC));

        view.repoName.setText(repository.generateId());
        view.repoDescription.setText(repository.getDescription());
    }

    @Override
    protected SearchRepositoryItemView createView(View view) {
        return new SearchRepositoryItemView(view);
    }
}
