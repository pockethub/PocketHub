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
package com.github.pockethub.ui.repo;

import android.content.Context;
import android.view.LayoutInflater;

import com.alorma.github.sdk.bean.dto.response.Contributor;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.util.AvatarLoader;

/**
 * List adapter for a list of contributors
 */
public class ContributorListAdapter extends SingleTypeAdapter<Contributor> {

    private final Context context;

    private final AvatarLoader avatars;

    /**
     * Create contributor list adapter
     *
     * @param context
     * @param elements
     * @param avatars
     */
    public ContributorListAdapter(final Context context,
            final Contributor[] elements, final AvatarLoader avatars) {
        super(LayoutInflater.from(context), R.layout.contributor_item);

        this.context = context.getApplicationContext();
        this.avatars = avatars;
        setItems(elements);
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).id;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.iv_avatar, R.id.tv_login, R.id.tv_contributions };
    }

    @Override
    protected void update(int position, Contributor contributor) {
        avatars.bind(imageView(0), contributor);
        setText(1, contributor.author.login);
        setText(2, context.getString(R.string.contributions, contributor.total));
    }
}
