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

import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.core.issue.IssueUtils;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.ViewUtils;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.Issue;

/**
 * Adapter for a list of {@link Issue} objects
 */
public class RepositoryIssueListAdapter extends
        IssueListAdapter<Issue, RepositoryIssueItemView> {

    /**
     * @param inflater
     * @param elements
     * @param avatars
     */
    public RepositoryIssueListAdapter(LayoutInflater inflater,
            Issue[] elements, AvatarLoader avatars) {
        super(layout.repo_issue_item, inflater, elements, avatars);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected void update(final int position,
            final RepositoryIssueItemView view, final Issue issue) {
        updateNumber(issue.getNumber(), issue.getState(),
                view.numberPaintFlags, view.number);

        avatars.bind(view.avatar, issue.getUser());

        ViewUtils.setGone(view.pullRequestIcon,
                !IssueUtils.isPullRequest(issue));

        view.title.setText(issue.getTitle());

        updateReporter(issue.getUser().getLogin(), issue.getCreatedAt(),
                view.reporter);
        updateComments(issue.getComments(), view.comments);
        updateLabels(issue.getLabels(), view.labels);
    }

    @Override
    protected RepositoryIssueItemView createView(View view) {
        return new RepositoryIssueItemView(view);
    }

    protected int getNumber(Issue issue) {
        return issue.getNumber();
    }
}
