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
import android.view.LayoutInflater;
import android.view.View;

import com.viewpagerindicator.R.layout;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Adapter for the default account's repositories
 */
public class DefaultRepositoryListAdapter extends RepositoryListAdapter<Repository, RecentRepositoryItemView> {

    private final AtomicReference<User> account;

    private final AtomicReference<RecentRepositories> recent;

    /**
     * Create list adapter for repositories
     *
     * @param inflater
     * @param elements
     * @param account
     * @param recent
     */
    public DefaultRepositoryListAdapter(LayoutInflater inflater, Repository[] elements, AtomicReference<User> account,
            AtomicReference<RecentRepositories> recent) {
        super(layout.repo_item, inflater, elements);

        this.account = account;
        this.recent = recent;
    }

    /**
     * Create list adapter for repositories
     *
     * @param inflater
     * @param account
     * @param recent
     */
    public DefaultRepositoryListAdapter(LayoutInflater inflater, AtomicReference<User> account,
            AtomicReference<RecentRepositories> recent) {
        this(inflater, null, account, recent);
    }

    @Override
    protected void update(final int position, final RecentRepositoryItemView view, final Repository repository) {
        view.recentLabel.setVisibility(recent.get().contains(repository.getId()) ? VISIBLE : GONE);

        view.repoName.setText(account.get().getLogin().equals(repository.getOwner().getLogin()) ? repository.getName()
                : repository.generateId());

        updateDetails(view, repository.getDescription(), repository.getLanguage(), repository.getWatchers(),
                repository.getForks(), repository.isPrivate(), repository.isFork());
    }

    @Override
    protected RecentRepositoryItemView createView(final View view) {
        return new RecentRepositoryItemView(view);
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).getId();
    }
}
