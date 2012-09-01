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

import android.view.LayoutInflater;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.util.AvatarLoader;

import org.eclipse.egit.github.core.User;

/**
 * List adapter for a list of users
 */
public class UserListAdapter extends SingleTypeAdapter<User> {

    private final AvatarLoader avatars;

    /**
     * Create user list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     */
    public UserListAdapter(final LayoutInflater inflater,
            final User[] elements, final AvatarLoader avatars) {
        super(inflater, layout.user_item);

        this.avatars = avatars;
        setItems(elements);
    }

    /**
     * Create user list adapter
     *
     * @param inflater
     * @param avatars
     */
    public UserListAdapter(final LayoutInflater inflater,
            final AvatarLoader avatars) {
        this(inflater, null, avatars);
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).getId();
    }

    protected int[] getChildViewIds() {
        return new int[] { id.iv_avatar, id.tv_login };
    }

    @Override
    protected void update(final int position, final User user) {
        avatars.bind(imageView(0), user);
        setText(1, user.getLogin());
    }
}
