/*
 * Copyright 2013 GitHub Inc.
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
package com.github.mobile.ui.search;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.accounts.AccountUtils;
import com.github.mobile.core.search.SearchUser;
import com.github.mobile.core.search.SearchUserService;
import com.github.mobile.core.user.RefreshUserTask;
import com.github.mobile.ui.ItemListFragment;
import org.eclipse.egit.github.core.User;
import com.github.mobile.R.string;
import com.github.mobile.ui.user.UserViewActivity;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.List;

import static android.app.SearchManager.QUERY;

/**
 * Fragment to display a list of {@link SearchUser} instances
 */
public class SearchUserListFragment extends ItemListFragment<SearchUser> {

    private String query;

    @Inject
    private SearchUserService service;

    @Inject
    private AvatarLoader avatars;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_people);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        query = getStringExtra(QUERY);
    }

    @Override
    public void refresh() {
        query = getStringExtra(QUERY);

        super.refresh();
    }

    @Override
    public Loader<List<SearchUser>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<SearchUser>>(getActivity(), items) {

            @Override
            public List<SearchUser> loadData() throws Exception {
                return service.searchUsers(query);
            }
        };
    }

    @Override
    protected SingleTypeAdapter<SearchUser> createAdapter(List<SearchUser> items) {
        return new SearchUserListAdapter(getActivity(),
            items.toArray(new SearchUser[items.size()]), avatars);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final SearchUser result = (SearchUser) l.getItemAtPosition(position);
        new RefreshUserTask(getActivity(), result.getLogin()) {

            @Override
            protected void onSuccess(User user) throws Exception {
                super.onSuccess(user);

                if (!AccountUtils.isUser(getActivity(), user))
                    startActivity(UserViewActivity.createIntent(user));
            }
        }.execute();
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return string.error_users_search;
    }
}
