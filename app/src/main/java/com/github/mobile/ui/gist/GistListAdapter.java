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
package com.github.mobile.ui.gist;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.TimeUtils;

import java.text.NumberFormat;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;

/**
 * Adapter to display a list of {@link Gist} objects
 */
public class GistListAdapter extends ItemListAdapter<Gist, GistView> {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    private final AvatarLoader avatarHelper;

    /**
     * @param avatarHelper
     * @param inflater
     * @param elements
     */
    public GistListAdapter(AvatarLoader avatarHelper, LayoutInflater inflater, Gist[] elements) {
        super(layout.gist_item, inflater, elements);

        this.avatarHelper = avatarHelper;
    }

    /**
     * @param avatarHelper
     * @param inflater
     */
    public GistListAdapter(AvatarLoader avatarHelper, LayoutInflater inflater) {
        this(avatarHelper, inflater, null);
    }

    @Override
    protected void update(final GistView view, final Gist gist) {
        view.gistId.setText(gist.getId());

        String description = gist.getDescription();
        if (!TextUtils.isEmpty(description))
            view.title.setText(description);
        else
            view.title.setText(string.no_description);

        User user = gist.getUser();

        if (user != null)
            view.author.setText(user.getLogin());
        else
            view.author.setText(string.anonymous);

        avatarHelper.bind(view.avatar, user);

        view.created.setText(TimeUtils.getRelativeTime(gist.getCreatedAt()));

        view.files.setText(NUMBER_FORMAT.format(gist.getFiles().size()));
        view.comments.setText(NUMBER_FORMAT.format(gist.getComments()));
    }

    @Override
    protected GistView createView(final View view) {
        return new GistView(view);
    }

    @Override
    public long getItemId(final int position) {
        final String id = getItem(position).getId();
        return !TextUtils.isEmpty(id) ? id.hashCode() : super.getItemId(position);
    }
}
