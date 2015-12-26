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
package com.github.pockethub.ui.commit;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.core.commit.CommitUtils;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.TypefaceUtils;

import java.util.Collection;

/**
 * Adapter to display commits
 */
public class CommitListAdapter extends SingleTypeAdapter<Commit> {

    private final AvatarLoader avatars;

    /**
     * @param viewId
     * @param inflater
     * @param elements
     * @param avatars
     */
    public CommitListAdapter(int viewId, LayoutInflater inflater,
            Collection<Commit> elements, AvatarLoader avatars) {
        super(inflater, viewId);

        this.avatars = avatars;
        setItems(elements);
    }

    @Override
    public long getItemId(int position) {
        String sha = getItem(position).sha;
        if (!TextUtils.isEmpty(sha))
            return sha.hashCode();
        else
            return super.getItemId(position);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.tv_commit_id, R.id.tv_commit_author, R.id.iv_avatar,
                R.id.tv_commit_message, R.id.tv_commit_comments };
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);

        TypefaceUtils.setOcticons((TextView) view
                .findViewById(R.id.tv_comment_icon));
        return view;
    }

    @Override
    protected void update(int position, Commit item) {
        setText(0, CommitUtils.abbreviate(item.sha));

        StyledText authorText = new StyledText();
        authorText.bold(CommitUtils.getAuthor(item));
        authorText.append(' ');
        authorText.append(CommitUtils.getAuthorDate(item));
        setText(1, authorText);

        CommitUtils.bindAuthor(item, avatars, imageView(2));
        setText(3, item.commit.message);
        setText(4, CommitUtils.getCommentCount(item));
    }
}
