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
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.core.issue.IssueUtils;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.TypefaceUtils;

import org.eclipse.egit.github.core.RepositoryIssue;

/**
 * Adapter to display a list of dashboard issues
 */
public class DashboardIssueListAdapter extends
        IssueListAdapter<RepositoryIssue> {

    private int numberPaintFlags;

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
    protected int getNumber(final RepositoryIssue issue) {
        return issue.getNumber();
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);

        numberPaintFlags = textView(view, 1).getPaintFlags();
        TypefaceUtils.setOcticons(textView(view, 6),
                (TextView) view.findViewById(id.tv_comment_icon));
        return view;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { id.tv_issue_repo_name, id.tv_issue_number,
                id.tv_issue_title, id.iv_avatar, id.tv_issue_creation,
                id.tv_issue_comments, id.tv_pull_request_icon, id.v_label0,
                id.v_label1, id.v_label2, id.v_label3, id.v_label4,
                id.v_label5, id.v_label6, id.v_label7 };
    }

    @Override
    protected void update(int position, RepositoryIssue issue) {
        updateNumber(issue.getNumber(), issue.getState(), numberPaintFlags, 1);

        avatars.bind(imageView(3), issue.getUser());

        String[] segments = issue.getUrl().split("/");
        int length = segments.length;
        if (length >= 4)
            setText(0, segments[length - 4] + '/' + segments[length - 3]);
        else
            setText(0, null);

        setGone(6, !IssueUtils.isPullRequest(issue));

        setText(2, issue.getTitle());

        updateReporter(issue.getUser().getLogin(), issue.getCreatedAt(), 4);
        setNumber(5, issue.getComments());
        updateLabels(issue.getLabels(), 7);
    }
}
