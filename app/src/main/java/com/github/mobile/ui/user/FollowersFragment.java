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
package com.github.mobile.ui.user;

import android.os.Bundle;

import com.github.mobile.R.string;
import com.github.mobile.core.ResourcePager;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;
import com.github.mobile.ui.PagedItemFragment;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.UserService;

/**
 * Fragment to display a list of followers
 */
public class FollowersFragment extends PagedItemFragment<User> {

    @Inject
    private AvatarLoader avatarHelper;

    @Inject
    private UserService userService;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListViewUtils.configure(getActivity(), getListView(), true);
    }

    @Override
    protected ResourcePager<User> createPager() {
        return new ResourcePager<User>() {

            protected Object getId(User resource) {
                return resource.getId();
            }

            public PageIterator<User> createIterator(int page, int size) {
                return userService.pageFollowers(page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_followers;
    }

    @Override
    protected ItemListAdapter<User, ? extends ItemView> createAdapter(List<User> items) {
        User[] users = items.toArray(new User[items.size()]);
        return new UserListAdapter(getActivity().getLayoutInflater(), users, avatarHelper);
    }
}
