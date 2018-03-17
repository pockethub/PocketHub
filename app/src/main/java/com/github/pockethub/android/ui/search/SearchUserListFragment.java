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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.ui.item.UserItem;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.SearchPage;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.ui.user.UserViewActivity;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.service.search.SearchService;
import com.meisolsson.githubsdk.service.users.UserService;
import com.xwray.groupie.Item;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static android.app.SearchManager.QUERY;

/**
 * Fragment to display a list of {@link User} instances
 */
public class SearchUserListFragment extends PagedItemFragment<User> {

    SearchService service = ServiceGenerator.createService(getContext(), SearchService.class);

    private String query;

    @Inject
    protected AvatarLoader avatars;

    @Override
    protected Single<Response<Page<User>>> loadData(int page) {
        return service.searchUsers(query, null, null, page)
                .map(response -> {
                    SearchPage<User> repositorySearchPage = response.body();

                    return Response.success(Page.<User>builder()
                            .first(repositorySearchPage.first())
                            .last(repositorySearchPage.last())
                            .next(repositorySearchPage.next())
                            .prev(repositorySearchPage.prev())
                            .items(repositorySearchPage.items())
                            .build());
                });
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_user;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_people);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        query = getStringExtra(QUERY);
    }

    @Override
    public void refresh() {
        query = getStringExtra(QUERY);

        super.refresh();
    }

    @Override
    protected Item createItem(User item) {
        return new UserItem(avatars, item);
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof UserItem) {
            User result = ((UserItem) item).getUser();
            ServiceGenerator.createService(getContext(), UserService.class)
                    .getUser(result.login())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(AutoDisposeUtils.bindToLifecycle(this))
                    .subscribe(response ->
                            startActivity(UserViewActivity.createIntent(response.body())));
        }
    }

    @Override
    protected int getErrorMessage() {
        return R.string.error_users_search;
    }
}
