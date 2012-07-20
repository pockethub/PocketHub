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

import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.core.commit.CommitUtils;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.StyledText;
import com.github.mobile.util.AvatarLoader;

import java.util.Collection;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.User;

/**
 *
 */
public class CommitListAdapter extends
        ItemListAdapter<RepositoryCommit, CommitView> {

    private final AvatarLoader avatars;

    /**
     * @param viewId
     * @param inflater
     * @param elements
     * @param avatars
     */
    public CommitListAdapter(int viewId, LayoutInflater inflater,
            Collection<RepositoryCommit> elements, AvatarLoader avatars) {
        this(viewId, inflater, elements != null ? elements
                .toArray(new RepositoryCommit[elements.size()]) : null, avatars);
    }

    /**
     * @param viewId
     * @param inflater
     * @param elements
     * @param avatars
     */
    public CommitListAdapter(int viewId, LayoutInflater inflater,
            RepositoryCommit[] elements, AvatarLoader avatars) {
        super(viewId, inflater, elements);

        this.avatars = avatars;
    }

    @Override
    protected void update(final int position, final CommitView view,
            final RepositoryCommit item) {
        view.sha.setText(CommitUtils.abbreviate(item.getSha()));

        User author = item.getAuthor();
        Commit commit = item.getCommit();
        CommitUser commitAuthor = commit.getAuthor();
        String name = author != null ? author.getLocation()
                : commitAuthor != null ? commitAuthor.getName() : null;
        StyledText authorText = new StyledText();
        authorText.bold(name);
        if (commitAuthor != null) {
            authorText.append(' ');
            authorText.append(commit.getAuthor().getDate());
        }
        view.author.setText(authorText);

        avatars.bind(view.avatar, author);

        view.message.setText(commit.getMessage());
    }

    @Override
    protected CommitView createView(final View view) {
        return new CommitView(view);
    }
}
