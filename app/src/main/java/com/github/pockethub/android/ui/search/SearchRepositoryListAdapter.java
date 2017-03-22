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
package com.github.pockethub.android.ui.search;

import android.text.TextUtils;
import android.view.LayoutInflater;

import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.repo.RepositoryListAdapter;

/**
 * Adapter for a list of searched for repositories
 */
public class SearchRepositoryListAdapter extends
        RepositoryListAdapter<Repository> {

    /**
     * Create list adapter for searched for repositories
     *
     * @param inflater
     * @param elements
     */
    public SearchRepositoryListAdapter(LayoutInflater inflater,
            Repository[] elements) {
        super(R.layout.user_repo_item, inflater, elements);
    }

    @Override
    public long getItemId(final int position) {
        final String id = String.valueOf(getItem(position).id());
        return !TextUtils.isEmpty(id) ? id.hashCode() : super
                .getItemId(position);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.tv_repo_icon, R.id.tv_repo_description,
                R.id.tv_language, R.id.tv_watchers, R.id.tv_forks, R.id.tv_repo_name };
    }

    @Override
    protected void update(int position, Repository repository) {
        StyledText name = new StyledText();
        name.append(repository.owner().login()).append('/');
        name.bold(repository.name());
        setText(5, name);

        updateDetails(repository.description(), repository.language(),
                repository.watchersCount(), repository.forksCount(),
                repository.isPrivate(), repository.isFork(), null);
    }
}
