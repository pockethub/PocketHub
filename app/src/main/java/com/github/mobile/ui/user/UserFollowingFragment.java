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
import android.app.Activity;

import com.github.mobile.core.ResourcePager;
import com.github.mobile.core.user.UserPager;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;

/**
 * Fragment to display the users being followed by a specific user
 */
public class UserFollowingFragment extends FollowingFragment {

    private User user;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        user = getSerializableExtra(EXTRA_USER);
    }

    @Override
    protected ResourcePager<User> createPager() {
        return new UserPager() {

            @Override
            public PageIterator<User> createIterator(int page, int size) {
                return service.pageFollowing(user.getLogin(), page, size);
            }
        };
    }
}
