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
package com.github.pockethub.android.ui.repo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.core.ResourcePager;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;

import java.util.List;

import static com.github.pockethub.android.Intents.EXTRA_USER;
import static com.github.pockethub.android.RequestCodes.REPOSITORY_VIEW;
import static com.github.pockethub.android.ResultCodes.RESOURCE_CHANGED;

/**
 * Fragment to display a list of repositories for a {@link User}
 */
public class UserRepositoryListFragment extends PagedItemFragment<Repository> {

    private User user;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        user = getParcelableExtra(EXTRA_USER);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_repositories);
    }

    @Override
    protected ResourcePager<Repository> createPager() {
        return new ResourcePager<Repository>() {

            @Override
            protected Object getId(Repository resource) {
                return resource.id();
            }

            @Override
            public PageIterator<Repository> createIterator(int page, int size) {
                return new PageIterator<>(page1 ->
                        ServiceGenerator.createService(getContext(), RepositoryService.class)
                                .getUserRepositories(user.login(), page1), page);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_repositories;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_repos_load;
    }

    @Override
    protected SingleTypeAdapter<Repository> createAdapter(List<Repository> items) {
        return new UserRepositoryListAdapter(getActivity().getLayoutInflater(),
                items.toArray(new Repository[items.size()]), user);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REPOSITORY_VIEW && resultCode == RESOURCE_CHANGED) {
            forceRefresh();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Repository repo = (Repository) list.getItemAtPosition(position);
        startActivityForResult(RepositoryViewActivity.createIntent(repo),
                REPOSITORY_VIEW);
    }
}
