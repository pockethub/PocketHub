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

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.R;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.util.TypefaceUtils;

/**
 * Adapter for a list of repositories
 */
public class UserRepositoryListAdapter extends
        RepositoryListAdapter<Repo> {

    private final String login;

    private int descriptionColor;

    /**
     * Create list adapter for repositories
     *
     * @param inflater
     * @param elements
     * @param user
     */
    public UserRepositoryListAdapter(LayoutInflater inflater,
            Repo[] elements, User user) {
        super(R.layout.user_repo_item, inflater, elements);

        login = user.login;
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).id;
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);

        TypefaceUtils.setOcticons(textView(view, 0),
                (TextView) view.findViewById(R.id.tv_forks_icon),
                (TextView) view.findViewById(R.id.tv_watchers_icon));
        descriptionColor = view.getResources().getColor(R.color.text_description);
        return view;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.tv_repo_icon, R.id.tv_repo_description,
                R.id.tv_language, R.id.tv_watchers, R.id.tv_forks, R.id.tv_repo_name };
    }

    @Override
    protected void update(int position, Repo repository) {
        StyledText name = new StyledText();
        if (!login.equals(repository.owner.login))
            name.foreground(repository.owner.login, descriptionColor)
                    .foreground('/', descriptionColor);
        name.bold(repository.name);
        setText(5, name);

        updateDetails(repository.description, repository.language,
                repository.watchers_count, repository.forks_count,
                repository.isPrivate, repository.fork,
                repository.mirror_url);
    }
}
