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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.ui.item.ContributorItem;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.user.UserViewActivity;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;
import com.meisolsson.githubsdk.service.users.UserService;
import com.xwray.groupie.Item;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;

/**
 * Fragment to display a list of contributors for a specific repository
 */
public class RepositoryContributorsFragment extends PagedItemFragment<User> {

    RepositoryService service = ServiceGenerator.createService(getActivity(), RepositoryService.class);

    /**
     * Avatar loader
     */
    @Inject
    protected AvatarLoader avatars;

    private Repository repo;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        repo = getParcelableExtra(EXTRA_REPOSITORY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_contributors);
    }

    @Override
    protected Single<Response<Page<User>>> loadData(int page) {
        return service.getContributors(repo.owner().login(), repo.name(), page);
    }

    @Override
    protected Item createItem(User item) {
        return new ContributorItem(avatars, item);
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof ContributorItem) {
            User contributor = ((ContributorItem) item).getData();
            ServiceGenerator.createService(getContext(), UserService.class)
                    .getUser(contributor.login())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(AutoDisposeUtils.bindToLifecycle(this))
                    .subscribe(response ->
                            startActivity(UserViewActivity.createIntent(response.body())));
        }
    }

    @Override
    protected int getErrorMessage() {
        return R.string.error_contributors_load;
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading;
    }
}
