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

import org.eclipse.egit.github.core.Issue;

/**
 * Adapter for a list of {@link Issue} objects
 */
public class RepositoryIssueListAdapter extends IssueListAdapter<Issue> {

    private int numberPaintFlags;

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
    protected View initialize(View view) {
        view = super.initialize(view);

        numberPaintFlags = textView(view, 0).getPaintFlags();
        TypefaceUtils.setOcticons(textView(view, 5),
                (TextView) view.findViewById(id.tv_comment_icon));
        return view;
    }

    protected int getNumber(Issue issue) {
        return issue.getNumber();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { id.tv_issue_number, id.tv_issue_title, id.iv_avatar,
                id.tv_issue_creation, id.tv_issue_comments,
                id.tv_pull_request_icon, id.v_label0, id.v_label1, id.v_label2,
                id.v_label3, id.v_label4, id.v_label5, id.v_label6, id.v_label7 };
    }

    @Override
    protected void update(int position, Issue issue) {
        updateNumber(issue.getNumber(), issue.getState(), numberPaintFlags, 0);

        avatars.bind(imageView(2), issue.getUser());

        setGone(5, !IssueUtils.isPullRequest(issue));

        setText(1, issue.getTitle());

        updateReporter(issue.getUser().getLogin(), issue.getCreatedAt(), 3);
        setNumber(4, issue.getComments());
        updateLabels(issue.getLabels(), 6);
    }
}
