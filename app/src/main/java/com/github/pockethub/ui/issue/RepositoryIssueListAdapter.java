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
package com.github.pockethub.ui.issue;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.github.pockethub.R;
import com.github.pockethub.core.issue.IssueUtils;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.TimeUtils;
import com.github.pockethub.util.TypefaceUtils;

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
        super(R.layout.repo_issue_item, inflater, elements, avatars);
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(getItem(position).id);
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);

        numberPaintFlags = textView(view, 0).getPaintFlags();
        TypefaceUtils.setOcticons(textView(view, 5),
                (TextView) view.findViewById(R.id.tv_comment_icon));
        return view;
    }

    @Override
    protected int getNumber(Issue issue) {
        return issue.number;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.tv_issue_number, R.id.tv_issue_title, R.id.iv_avatar,
                R.id.tv_issue_creation, R.id.tv_issue_comments,
                R.id.tv_pull_request_icon, R.id.v_label0, R.id.v_label1, R.id.v_label2,
                R.id.v_label3, R.id.v_label4, R.id.v_label5, R.id.v_label6, R.id.v_label7 };
    }

    @Override
    protected void update(int position, Issue issue) {
        updateNumber(issue.number, issue.state, numberPaintFlags, 0);

        avatars.bind(imageView(2), issue.user);

        setGone(5, !IssueUtils.isPullRequest(issue));

        setText(1, issue.title);

        updateReporter(issue.user.login, TimeUtils.stringToDate(issue.created_at), 3);
        setNumber(4, issue.comments);
        updateLabels(issue.labels, 6);
    }
}
