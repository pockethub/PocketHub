/*
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.R;
import com.github.mobile.core.search.SearchUser;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.util.AvatarLoader;

import java.util.List;

/**
 * Adapter for a list of searched users
 */
public class SearchUserListAdapter extends ItemListAdapter<SearchUser, SearchUserItemView> {

    private final AvatarLoader avatars;

    /**
     * Create user list adapter
     *
     * @param context
     * @param elements
     * @param avatars
     */
    public SearchUserListAdapter(final Context context,
            final List<SearchUser> elements, final AvatarLoader avatars) {
        super(R.layout.user_item, LayoutInflater.from(context), elements);

        this.avatars = avatars;
        setItems(elements);
    }

    @Override
    public long getItemId(final int position) {
        String userId = getItem(position).getId();
        return Long.parseLong(userId.replace("user-", ""));
    }

    @Override
    protected void update(final int position, final SearchUserItemView view,
        final SearchUser user) {
        avatars.bind(view.avatarView, user);
        view.loginView.setText(user.getLogin());
    }

    @Override
    protected SearchUserItemView createView(final View view) {
        return new SearchUserItemView(view);
    }
}
