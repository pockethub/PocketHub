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
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.ui.StyledText;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.TypefaceUtils;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;

/**
 * Adapter to display a list of {@link Gist} objects
 */
public class GistListAdapter extends SingleTypeAdapter<Gist> {

    private final AvatarLoader avatars;

    private String anonymous;

    /**
     * @param avatars
     * @param inflater
     * @param elements
     */
    public GistListAdapter(AvatarLoader avatars, LayoutInflater inflater,
            Gist[] elements) {
        super(inflater, layout.gist_item);

        this.avatars = avatars;
        setItems(elements);
    }

    @Override
    public long getItemId(final int position) {
        final String id = getItem(position).getId();
        return !TextUtils.isEmpty(id) ? id.hashCode() : super
                .getItemId(position);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { id.tv_gist_id, id.tv_gist_title, id.tv_gist_author,
                id.tv_gist_comments, id.tv_gist_files, id.iv_avatar };
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);

        TypefaceUtils.setOcticons(
                (TextView) view.findViewById(id.tv_comment_icon),
                (TextView) view.findViewById(id.tv_file_icon));
        anonymous = view.getResources().getString(string.anonymous);
        return view;
    }

    @Override
    protected void update(int position, Gist gist) {
        setText(0, gist.getId());

        String description = gist.getDescription();
        if (!TextUtils.isEmpty(description))
            setText(1, description);
        else
            setText(1, string.no_description_given);

        User user = gist.getUser();
        avatars.bind(imageView(5), user);

        StyledText authorText = new StyledText();
        if (user != null)
            authorText.bold(user.getLogin());
        else
            authorText.bold(anonymous);
        authorText.append(' ');
        authorText.append(gist.getCreatedAt());
        setText(2, authorText);

        setNumber(3, gist.getComments());
        setNumber(4, gist.getFiles().size());
    }
}
