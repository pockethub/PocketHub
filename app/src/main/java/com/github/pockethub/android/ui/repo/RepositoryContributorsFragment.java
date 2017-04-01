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
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ThrowableLoader;
import com.github.pockethub.android.ui.ItemListFragment;
import com.github.pockethub.android.ui.user.UserViewActivity;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;
import com.meisolsson.githubsdk.service.users.UserService;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;

/**
 * Fragment to display a list of contributors for a specific repository
 */
public class RepositoryContributorsFragment extends ItemListFragment<User> {

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
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<User>>(getActivity(), items) {

            @Override
            public List<User> loadData() throws Exception {
                RepositoryService service = ServiceGenerator.createService(getActivity(), RepositoryService.class);

                int current = 1;
                int last = 0;
                List<User> users = new ArrayList<>();

                while (current != last) {
                    Page<User> page = service
                            .getContributors(repo.owner().login(), repo.name(), current)
                            .blockingGet()
                            .body();

                    users.addAll(page.items());
                    last = page.last() != null ? page.last() : -1;
                    current = page.next() != null ? page.next() : -1;
                }
                return users;
            }
        };
    }

    @Override
    protected SingleTypeAdapter<User> createAdapter(List<User> items) {
        return new ContributorListAdapter(getActivity(),
                items.toArray(new User[items.size()]), avatars);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final User contributor = (User) l.getItemAtPosition(position);
        ServiceGenerator.createService(getContext(), UserService.class)
                .getUser(contributor.login())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(response ->
                        startActivity(UserViewActivity.createIntent(response.body())));

    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_contributors_load;
    }
}
