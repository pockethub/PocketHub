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
package com.github.mobile.ui.issue;

import static android.view.View.GONE;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.ViewUtils;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.SearchIssue;
import org.eclipse.egit.github.core.User;

/**
 * Adapter for a list of searched for issues
 */
public class SearchIssueListAdapter extends
        IssueListAdapter<SearchIssue, RepositoryIssueItemView> {

    /**
     * @param inflater
     * @param elements
     * @param avatars
     */
    public SearchIssueListAdapter(LayoutInflater inflater,
            SearchIssue[] elements, AvatarLoader avatars) {
        super(layout.repo_issue_item, inflater, elements, avatars);
    }

    @Override
    protected void update(final int position,
            final RepositoryIssueItemView view, final SearchIssue issue) {
        updateNumber(issue.getNumber(), issue.getState(),
                view.numberPaintFlags, view.number);

        String gravatarId = issue.getGravatarId();
        if (!TextUtils.isEmpty(gravatarId)) {
            User user = new User();
            user.setGravatarId(gravatarId);
            avatars.bind(view.avatar, user);
        } else
            avatars.bind(view.avatar, null);

        ViewUtils.setGone(view.pullRequestIcon, true);

        view.title.setText(issue.getTitle());

        updateReporter(issue.getUser(), issue.getCreatedAt(), view.reporter);
        updateComments(issue.getComments(), view.comments);
    }

    @Override
    protected RepositoryIssueItemView createView(View view) {
        RepositoryIssueItemView itemView = new RepositoryIssueItemView(view);
        for (View label : itemView.labels)
            label.setVisibility(GONE);
        return itemView;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getNumber();
    }

    @Override
    protected int getNumber(SearchIssue issue) {
        return issue.getNumber();
    }
}
