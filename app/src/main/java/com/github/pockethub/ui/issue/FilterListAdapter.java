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
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.Label;
import com.alorma.github.sdk.bean.dto.response.Milestone;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.R;
import com.github.pockethub.core.issue.IssueFilter;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.InfoUtils;

import java.util.Collection;

/**
 * Adapter to display a list of {@link IssueFilter} objects
 */
public class FilterListAdapter extends SingleTypeAdapter<IssueFilter> {

    private final AvatarLoader avatars;

    /**
     * Create {@link IssueFilter} list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     */
    public FilterListAdapter(LayoutInflater inflater, IssueFilter[] elements,
            AvatarLoader avatars) {
        super(inflater, R.layout.issues_filter_item);

        this.avatars = avatars;
        setItems(elements);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.iv_avatar, R.id.tv_repo_name, R.id.tv_filter_state,
                R.id.tv_filter_labels, R.id.tv_filter_milestone, R.id.ll_assignee,
                R.id.tv_filter_assignee, R.id.iv_assignee_avatar };
    }

    @Override
    protected void update(int position, IssueFilter filter) {
        avatars.bind(imageView(0), filter.getRepository().owner);
        setText(1, InfoUtils.createRepoId(filter.getRepository()));
        if (filter.isOpen())
            setText(2, R.string.open_issues);
        else
            setText(2, R.string.closed_issues);

        Collection<Label> labels = filter.getLabels();
        if (labels != null && !labels.isEmpty()) {
            TextView labelsText = textView(3);
            LabelDrawableSpan.setText(labelsText, labels);
            ViewUtils.setGone(labelsText, false);
        } else
            setGone(3, true);

        Milestone milestone = filter.getMilestone();
        if (milestone != null)
            ViewUtils.setGone(setText(4, milestone.title), false);
        else
            setGone(4, true);

        User assignee = filter.getAssignee();
        if (assignee != null) {
            avatars.bind(imageView(7), assignee);
            ViewUtils.setGone(setText(6, assignee.login), false);
        } else
            setGone(5, true);
    }
}
