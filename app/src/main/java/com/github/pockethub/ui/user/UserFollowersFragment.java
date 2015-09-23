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
package com.github.pockethub.ui.user;

import static com.github.pockethub.Intents.EXTRA_USER;
import android.app.Activity;

import com.alorma.github.sdk.services.client.GithubClient;
import com.alorma.github.sdk.services.user.UserFollowersClient;
import com.github.pockethub.core.ResourcePager;
import com.github.pockethub.core.user.UserPager;

import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.core.PageIterator;

import java.util.List;

/**
 * Fragment to display a list of followers
 */
public class UserFollowersFragment extends FollowersFragment {

    private User user;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        user = getParcelableExtra(EXTRA_USER);
    }

    @Override
    protected ResourcePager<User> createPager() {
        return new UserPager() {

            @Override
            public PageIterator<User> createIterator(int page, int size) {
                return new PageIterator<>(new PageIterator.GitHubRequest<List<User>>() {
                    @Override
                    public GithubClient<List<User>> execute(int page) {
                        return new UserFollowersClient(getActivity(), user.login, page);
                    }
                }, page);
            }
        };
    }
}
