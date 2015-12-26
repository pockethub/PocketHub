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
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.R;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.TimeUtils;
import com.github.pockethub.util.TypefaceUtils;

/**
 * Adapter for a list of searched for issues
 */
public class SearchIssueListAdapter extends IssueListAdapter<Issue> {

    private int numberPaintFlags;

    /**
     * @param inflater
     * @param elements
     * @param avatars
     */
    public SearchIssueListAdapter(LayoutInflater inflater, Issue[] elements,
                                  AvatarLoader avatars) {
        super(R.layout.repo_issue_item, inflater, elements, avatars);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).number;
    }

    @Override
    protected int getNumber(Issue issue) {
        return issue.number;
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);

        numberPaintFlags = textView(view, 0).getPaintFlags();
        TypefaceUtils.setOcticons(
                (TextView) view.findViewById(R.id.tv_pull_request_icon),
                (TextView) view.findViewById(R.id.tv_comment_icon));
        for (int i = 0; i < MAX_LABELS; i++)
            ViewUtils.setGone(view.findViewById(R.id.v_label0 + i), true);
        ViewUtils.setGone(view.findViewById(R.id.tv_pull_request_icon), true);
        return view;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.tv_issue_number, R.id.tv_issue_title, R.id.iv_avatar,
                R.id.tv_issue_creation, R.id.tv_issue_comments };
    }

    @Override
    protected void update(int position, Issue issue) {
        updateNumber(issue.number, issue.state, numberPaintFlags, 0);

        avatars.bind(imageView(2), issue.user);

        setText(1, issue.title);

        updateReporter(issue.user.login, TimeUtils.stringToDate(issue.created_at), 3);
        setNumber(4, issue.comments);
    }
}
