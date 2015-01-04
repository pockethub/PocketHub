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
package com.github.mobile.ui.commit;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.R;
import com.github.mobile.core.commit.CommitUtils;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.StyledText;
import com.github.mobile.util.AvatarLoader;

import java.util.List;

import org.eclipse.egit.github.core.RepositoryCommit;

/**
 * Adapter to display commits
 */
public class CommitListAdapter extends ItemListAdapter<RepositoryCommit, CommitItemView> {

    private final AvatarLoader avatars;

    /**
     * @param inflater
     * @param elements
     * @param avatars
     */
    public CommitListAdapter(LayoutInflater inflater,
            List<RepositoryCommit> elements, AvatarLoader avatars) {
        super(R.layout.commit_item, inflater, elements);

        this.avatars = avatars;
        setItems(elements);
    }

    @Override
    public long getItemId(int position) {
        String sha = getItem(position).getSha();
        if (!TextUtils.isEmpty(sha))
            return sha.hashCode();
        else
            return super.getItemId(position);
    }

    @Override
    protected void update(final int position, final CommitItemView view,
        final RepositoryCommit item) {
        view.idView.setText(CommitUtils.abbreviate(item.getSha()));

        StyledText authorText = new StyledText();
        authorText.bold(CommitUtils.getAuthor(item));
        authorText.append(' ');
        authorText.append(CommitUtils.getAuthorDate(item));
        view.authorView.setText(authorText);

        CommitUtils.bindAuthor(item, avatars, view.avatarView);
        view.messageView.setText(item.getCommit().getMessage());
        view.commentView.setText(CommitUtils.getCommentCount(item));
    }

    @Override
    protected CommitItemView createView(final View view) {
        return new CommitItemView(view);
    }
}
