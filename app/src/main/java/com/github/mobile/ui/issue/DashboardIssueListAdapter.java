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

import com.github.mobile.R.layout;
import com.github.mobile.core.issue.IssueUtils;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.ViewUtils;

import org.eclipse.egit.github.core.RepositoryIssue;

/**
 * Adapter to display a list of dashboard issues
 */
public class DashboardIssueListAdapter extends
        IssueListAdapter<RepositoryIssue, DashboardIssueView> {

    /**
     * Create adapter
     *
     * @param avatars
     * @param inflater
     * @param elements
     */
    public DashboardIssueListAdapter(AvatarLoader avatars,
            LayoutInflater inflater, RepositoryIssue[] elements) {
        super(layout.dashboard_issue_item, inflater, elements, avatars);
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).getId();
    }

    @Override
    protected void update(final int position, final DashboardIssueView view,
            final RepositoryIssue issue) {
        updateNumber(issue.getNumber(), issue.getState(),
                view.numberPaintFlags, view.number);

        avatars.bind(view.avatar, issue.getUser());

        String[] segments = issue.getUrl().split("/");
        int length = segments.length;
        if (length >= 4)
            view.repoText.setText(segments[length - 4] + "/"
                    + segments[length - 3]);
        else
            view.repoText.setText("");

        ViewUtils.setGone(view.pullRequestIcon,
                !IssueUtils.isPullRequest(issue));

        view.title.setText(issue.getTitle());

        updateReporter(issue.getUser().getLogin(), issue.getCreatedAt(),
                view.reporter);
        updateComments(issue.getComments(), view.comments);
        updateLabels(issue.getLabels(), view.labels);
    }

    @Override
    protected DashboardIssueView createView(final View view) {
        return new DashboardIssueView(view);
    }

    @Override
    protected int getNumber(final RepositoryIssue issue) {
        return issue.getNumber();
    }
}
