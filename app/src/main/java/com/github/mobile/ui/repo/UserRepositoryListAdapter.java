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
import android.view.View;

import com.github.mobile.ui.ItemListAdapter;
import com.viewpagerindicator.R.layout;

import java.text.NumberFormat;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Adapter for a list of repositories
 */
public class UserRepositoryListAdapter extends ItemListAdapter<Repository, RepositoryItemView> {

    private static final NumberFormat FORMAT = NumberFormat.getIntegerInstance();

    private final String login;

    /**
     * Create list adapter for repositories
     *
     * @param inflater
     * @param elements
     * @param user
     */
    public UserRepositoryListAdapter(LayoutInflater inflater, Repository[] elements, User user) {
        super(layout.user_repo_item, inflater, elements);

        login = user.getLogin();
    }

    /**
     * Create list adapter for repositories
     *
     * @param inflater
     * @param user
     */
    public UserRepositoryListAdapter(LayoutInflater inflater, User user) {
        this(inflater, null, user);
    }

    @Override
    protected void update(final RepositoryItemView view, final Repository repository) {
        if (repository.isPrivate())
            view.repoIcon.setText(Character.toString(ICON_PRIVATE));
        else if (repository.isFork())
            view.repoIcon.setText(Character.toString(ICON_FORK));
        else
            view.repoIcon.setText(Character.toString(ICON_PUBLIC));

        view.repoName.setText(login.equals(repository.getOwner().getLogin()) ? repository.getName() : repository
                .generateId());

        String description = repository.getDescription();
        if (!TextUtils.isEmpty(description)) {
            view.repoDescription.setText(repository.getDescription());
            view.repoDescription.setVisibility(VISIBLE);
        } else
            view.repoDescription.setVisibility(GONE);

        String language = repository.getLanguage();
        if (TextUtils.isEmpty(language))
            view.language.setVisibility(GONE);
        else {
            view.language.setText(language);
            view.language.setVisibility(VISIBLE);
        }
        view.watchers.setText(FORMAT.format(repository.getWatchers()));
        view.forks.setText(FORMAT.format(repository.getForks()));
    }

    @Override
    protected RepositoryItemView createView(final View view) {
        return new RepositoryItemView(view);
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).getId();
    }
}
