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
import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.ui.item.repository.RepositoryItem;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;
import com.xwray.groupie.Item;

import io.reactivex.Single;
import retrofit2.Response;

import static com.github.pockethub.android.Intents.EXTRA_USER;
import static com.github.pockethub.android.RequestCodes.REPOSITORY_VIEW;
import static com.github.pockethub.android.ResultCodes.RESOURCE_CHANGED;

/**
 * Fragment to display a list of repositories for a {@link User}
 */
public class UserRepositoryListFragment extends PagedItemFragment<Repository> {

    RepositoryService service = ServiceGenerator.createService(getContext(), RepositoryService.class);

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
    protected Single<Response<Page<Repository>>> loadData(int page) {
        return service.getUserRepositories(user.login(), page);
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_repositories;
    }

    @Override
    protected int getErrorMessage() {
        return R.string.error_repos_load;
    }

    @Override
    protected Item createItem(Repository item) {
        return new RepositoryItem(item, user);
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
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof RepositoryItem) {
            Repository repo = ((RepositoryItem) item).getRepo();
            startActivityForResult(RepositoryViewActivity.createIntent(repo), REPOSITORY_VIEW);
        }
    }
}
