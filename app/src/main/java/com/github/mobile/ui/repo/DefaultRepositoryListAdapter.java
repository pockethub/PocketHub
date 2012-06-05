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
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;

import com.actionbarsherlock.R.color;
import com.github.mobile.ui.StyledText;
import com.viewpagerindicator.R.layout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Adapter for the default account's repositories
 */
public class DefaultRepositoryListAdapter extends RepositoryListAdapter<Repository, RepositoryHeaderItemView> {

    private final AtomicReference<User> account;

    private final Map<Long, String> headers = new HashMap<Long, String>();

    private final Set<Long> noSeparators = new HashSet<Long>();

    /**
     * Create list adapter for repositories
     *
     * @param inflater
     * @param elements
     * @param account
     */
    public DefaultRepositoryListAdapter(LayoutInflater inflater, Repository[] elements, AtomicReference<User> account) {
        super(layout.repo_item, inflater, elements);

        this.account = account;
    }

    /**
     * Create list adapter for repositories
     *
     * @param inflater
     * @param account
     */
    public DefaultRepositoryListAdapter(LayoutInflater inflater, AtomicReference<User> account) {
        this(inflater, null, account);
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
     * @param previous
     * @param text
     * @return this adapter
     */
    public DefaultRepositoryListAdapter registerHeader(Repository repository, Repository previous, String text) {
        headers.put(repository.getId(), text);
        if (previous != null)
            noSeparators.add(previous.getId());
        return this;
    }

    @Override
    protected void update(final int position, final RepositoryHeaderItemView view, final Repository repository) {
        String headerValue = headers.get(repository.getId());
        if (headerValue != null) {
            view.header.setVisibility(VISIBLE);
            view.headerText.setText(headerValue);
        } else
            view.header.setVisibility(GONE);

        if (noSeparators.contains(repository.getId()))
            view.separator.setVisibility(GONE);
        else
            view.separator.setVisibility(VISIBLE);

        StyledText name = new StyledText();
        if (!account.get().getLogin().equals(repository.getOwner().getLogin())) {
            ColorStateList states = view.repoName.getResources().getColorStateList(color.text_description_selector);
            name.foreground(repository.getOwner().getLogin(), states).foreground('/', states);
        }
        name.bold(repository.getName());
        view.repoName.setText(name);

        updateDetails(view, repository.getDescription(), repository.getLanguage(), repository.getWatchers(),
                repository.getForks(), repository.isPrivate(), repository.isFork());
    }

    @Override
    protected RepositoryHeaderItemView createView(final View view) {
        return new RepositoryHeaderItemView(view);
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).getId();
    }
}
