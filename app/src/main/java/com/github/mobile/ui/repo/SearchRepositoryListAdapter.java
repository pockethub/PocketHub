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

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.ui.StyledText;
import com.github.mobile.util.TypefaceUtils;
import com.viewpagerindicator.R.id;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.SearchRepository;

/**
 * Adapter for a list of searched for repositories
 */
public class SearchRepositoryListAdapter extends
        RepositoryListAdapter<SearchRepository> {

    /**
     * Create list adapter for searched for repositories
     *
     * @param inflater
     * @param elements
     */
    public SearchRepositoryListAdapter(LayoutInflater inflater,
            SearchRepository[] elements) {
        super(layout.user_repo_item, inflater, elements);
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
    public long getItemId(final int position) {
        final String id = getItem(position).getId();
        return !TextUtils.isEmpty(id) ? id.hashCode() : super
                .getItemId(position);
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);

        TypefaceUtils.setOcticons(textView(view, id.tv_repo_icon),
                (TextView) view.findViewById(id.tv_forks_icon),
                (TextView) view.findViewById(id.tv_watchers_icon));
        return view;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { id.tv_repo_icon, id.tv_repo_name,
                id.tv_repo_description, id.tv_language, id.tv_watchers,
                id.tv_forks };
    }

    @Override
    protected void update(int position, SearchRepository repository) {
        StyledText name = new StyledText();
        name.append(repository.getOwner()).append('/');
        name.bold(repository.getName());
        setText(id.tv_repo_name, name);

        updateDetails(repository.getDescription(), repository.getLanguage(),
                repository.getWatchers(), repository.getForks(),
                repository.isPrivate(), repository.isFork(), null);
    }
}
