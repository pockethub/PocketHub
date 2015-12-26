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
package com.github.pockethub.ui.gist;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.TimeUtils;
import com.github.pockethub.util.TypefaceUtils;

import java.util.Collection;

/**
 * Adapter to display a list of {@link Gist} objects
 */
public class GistListAdapter extends SingleTypeAdapter<Gist> {

    private final AvatarLoader avatars;

    private String anonymous;

    /**
     * @param avatars
     * @param activity
     * @param elements
     */
    public GistListAdapter(AvatarLoader avatars, Activity activity,
            Collection<Gist> elements) {
        super(activity, R.layout.gist_item);

        this.avatars = avatars;
        setItems(elements);
    }

    @Override
    public long getItemId(final int position) {
        final String id = getItem(position).id;
        return !TextUtils.isEmpty(id) ? id.hashCode() : super
                .getItemId(position);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.tv_gist_id, R.id.tv_gist_title, R.id.tv_gist_author,
                R.id.tv_gist_comments, R.id.tv_gist_files, R.id.iv_avatar };
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);

        TypefaceUtils.setOcticons(
                (TextView) view.findViewById(R.id.tv_comment_icon),
                (TextView) view.findViewById(R.id.tv_file_icon));
        anonymous = view.getResources().getString(R.string.anonymous);
        return view;
    }

    @Override
    protected void update(int position, Gist gist) {
        setText(0, gist.id);

        String description = gist.description;
        if (!TextUtils.isEmpty(description))
            setText(1, description);
        else
            setText(1, R.string.no_description_given);

        User user = gist.owner;
        avatars.bind(imageView(5), user);

        StyledText authorText = new StyledText();
        if (user != null)
            authorText.bold(user.login);
        else
            authorText.bold(anonymous);
        authorText.append(' ');
        authorText.append(TimeUtils.stringToDate(gist.created_at));
        setText(2, authorText);

        setNumber(3, gist.comments);
        setNumber(4, gist.files.size());
    }
}
