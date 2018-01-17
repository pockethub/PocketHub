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
package com.github.pockethub.android.ui.user;

import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.ui.item.UserItem;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.util.AvatarLoader;
import com.xwray.groupie.Item;

import javax.inject.Inject;

/**
 * Fragment to page over users
 */
public abstract class PagedUserFragment extends PagedItemFragment<User> {

    /**
     * Avatar loader
     */
    @Inject
    protected AvatarLoader avatars;

    @Override
    protected Item createItem(User item) {
        return new UserItem(avatars, item);
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof UserItem) {
            User user = ((UserItem) item).getData();
            startActivity(UserViewActivity.createIntent(user));
        }
    }
}
