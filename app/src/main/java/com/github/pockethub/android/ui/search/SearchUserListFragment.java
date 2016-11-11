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
import android.view.View;
import android.widget.ListView;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.SearchPage;
import com.meisolsson.githubsdk.model.User;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.accounts.AccountUtils;
import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.core.ResourcePager;
import com.github.pockethub.android.core.search.SearchUser;
import com.github.pockethub.android.rx.ObserverAdapter;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.ui.user.UserViewActivity;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.service.search.SearchService;
import com.meisolsson.githubsdk.service.users.UserService;
import com.google.inject.Inject;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.app.SearchManager.QUERY;

/**
 * Fragment to display a list of {@link SearchUser} instances
 */
public class SearchUserListFragment extends PagedItemFragment<User> {

    private String query;

    @Inject
    private AvatarLoader avatars;

    @Override
    protected ResourcePager<User> createPager() {
        return new ResourcePager<User>() {
            @Override
            protected Object getId(User resource) {
                return resource.id();
            }

            @Override
            public PageIterator<User> createIterator(int page, int size) {
                return new PageIterator<>(new PageIterator.GitHubRequest<Page<User>>() {
                    @Override
                    public Observable<Page<User>> execute(int page) {
                        return ServiceGenerator.createService(getContext(), SearchService.class)
                                .searchUsers(query, null, null, page)
                                .map(new Func1<SearchPage<User>, Page<User>>() {
                                    @Override
                                    public Page<User> call(SearchPage<User> userSearchPage) {
                                        return Page.<User>builder()
                                                .first(userSearchPage.first())
                                                .last(userSearchPage.last())
                                                .next(userSearchPage.next())
                                                .prev(userSearchPage.prev())
                                                .items(userSearchPage.items())
                                                .build();
                                    }
                                });
                    }
                }, page);
            }
        };
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
    protected SingleTypeAdapter<User> createAdapter(List<User> items) {
        return new SearchUserListAdapter(getActivity(),
                items.toArray(new User[items.size()]), avatars);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final User result = (User) l.getItemAtPosition(position);
        ServiceGenerator.createService(getContext(), UserService.class)
                .getUser(result.login())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<User>bindToLifecycle())
                .subscribe(new ObserverAdapter<User>() {

                    @Override
                    public void onNext(User user) {
                        if (!AccountUtils.isUser(getActivity(), user))
                            startActivity(UserViewActivity.createIntent(user));
                    }
                });
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_users_search;
    }
}
