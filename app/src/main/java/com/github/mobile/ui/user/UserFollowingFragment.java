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

import static com.github.mobile.Intents.EXTRA_USER;

import com.github.mobile.core.ResourcePager;
import com.github.mobile.core.user.UserPager;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;

import roboguice.inject.InjectExtra;

/**
 * Fragment to display the users being followed by a specific user
 */
public class UserFollowingFragment extends FollowingFragment {

    @InjectExtra(EXTRA_USER)
    private User user;

    @Inject
    protected ResourcePager<User> createPager() {
        return new UserPager() {

            @Override
            public PageIterator<User> createIterator(int page, int size) {
                return service.pageFollowing(user.getLogin(), page, size);
            }
        };
    }
}
