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

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.Intents;
import com.github.mobile.R.string;
import com.github.mobile.core.ResourcePager;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;
import com.github.mobile.ui.PagedItemFragment;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;

import roboguice.inject.InjectExtra;

/**
 * Fragment to display a list of repositories for a {@link User}
 */
public class UserRepositoryListFragment extends PagedItemFragment<Repository> {

    @Inject
    private RepositoryService service;

    @InjectExtra(Intents.EXTRA_USER)
    private User user;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListViewUtils.configure(getActivity(), getListView(), true);
    }

    @Override
    protected ResourcePager<Repository> createPager() {
        return new ResourcePager<Repository>() {

            @Override
            protected Object getId(Repository resource) {
                return resource.getId();
            }

            @Override
            public PageIterator<Repository> createIterator(int page, int size) {
                try {
                    return service.pageRepositories(user.getLogin(), page, size);
                } catch (IOException ignored) {
                    // TODO This is never actually thrown even though it is declared
                    return null;
                }
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_repositories;
    }

    @Override
    protected ItemListAdapter<Repository, ? extends ItemView> createAdapter(List<Repository> items) {
        return new UserRepositoryListAdapter(getActivity().getLayoutInflater(), items.toArray(new Repository[items
                .size()]), user);
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Repository repo = (Repository) list.getItemAtPosition(position);
        startActivity(RepositoryViewActivity.createIntent(repo));
    }
}
