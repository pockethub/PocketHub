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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Adapter for the default account's repositories
 */
public class DefaultRepositoryListAdapter extends
        RepositoryListAdapter<Repo> {

    private int descriptionColor;

    private final AtomicReference<User> account;

    private final Map<Long, String> headers = new HashMap<>();

    private final Set<Long> noSeparators = new HashSet<>();

    /**
     * Create list adapter for repositories
     *
     * @param inflater
     * @param elements
     * @param account
     */
    public DefaultRepositoryListAdapter(LayoutInflater inflater,
            Repo[] elements, AtomicReference<User> account) {
        super(R.layout.repo_item, inflater, elements);

        this.account = account;
    }

    /**
     * Clear registered header values
     *
     * @return this adapter
     */
    public DefaultRepositoryListAdapter clearHeaders() {
        headers.clear();
        noSeparators.clear();
        return this;
    }

    /**
     * Register section header
     *
     * @param repository
     * @param text
     * @return this adapter
     */
    public DefaultRepositoryListAdapter registerHeader(Repo repository,
            String text) {
        headers.put(repository.id, text);
        return this;
    }

    /**
     * Register repository to have no bottom separator
     *
     * @param repository
     * @return this adapter
     */
    public DefaultRepositoryListAdapter registerNoSeparator(
            Repo repository) {
        noSeparators.add(repository.id);
        return this;
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
                R.id.tv_language, R.id.tv_watchers, R.id.tv_forks, R.id.ll_header,
                R.id.tv_header, R.id.v_separator, R.id.tv_repo_name };
    }

    @Override
    protected void update(int position, Repo repository) {
        String headerValue = headers.get(repository.id);
        if (headerValue != null) {
            setGone(5, false);
            setText(6, headerValue);
        } else
            setGone(5, true);

        setGone(7, noSeparators.contains(repository.id));

        StyledText name = new StyledText();
        if (!account.get().login.equals(repository.owner.login))
            name.foreground(repository.owner.login, descriptionColor)
                    .foreground('/', descriptionColor);
        name.bold(repository.name);
        setText(8, name);

        updateDetails(repository.description, repository.language,
                repository.watchers_count, repository.forks_count,
                repository.isPrivate, repository.fork,
                repository.mirror_url);
    }
}
