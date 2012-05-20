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
import static android.view.View.VISIBLE;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.core.issue.IssueFilter;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.util.AvatarLoader;

import java.util.Collection;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

/**
 * Adapter to display a list of {@link IssueFilter} objects
 */
public class FilterListAdapter extends ItemListAdapter<IssueFilter, FilterItemView> {

    private final AvatarLoader avatars;

    /**
     * Create {@link IssueFilter} list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     */
    public FilterListAdapter(LayoutInflater inflater, IssueFilter[] elements, AvatarLoader avatars) {
        super(layout.issue_filter_item, inflater, elements);

        this.avatars = avatars;
    }

    /**
     * Create {@link IssueFilter} list adapter
     *
     * @param inflater
     * @param avatars
     */
    public FilterListAdapter(LayoutInflater inflater, AvatarLoader avatars) {
        this(inflater, null, avatars);
    }

    @Override
    protected void update(final int position, final FilterItemView view, final IssueFilter filter) {
        avatars.bind(view.avatarView, filter.getRepository().getOwner());
        view.repoText.setText(filter.getRepository().generateId());
        if (filter.isOpen())
            view.stateText.setText(string.open_issues);
        else
            view.stateText.setText(string.closed_issues);

        Collection<Label> labels = filter.getLabels();
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(view.labelsText, labels);
            view.labelsText.setVisibility(VISIBLE);
        } else
            view.labelsText.setVisibility(GONE);

        Milestone milestone = filter.getMilestone();
        if (milestone != null) {
            view.milestoneText.setText(milestone.getTitle());
            view.milestoneText.setVisibility(VISIBLE);
        } else
            view.milestoneText.setVisibility(GONE);

        User assignee = filter.getAssignee();
        if (assignee != null) {
            avatars.bind(view.assigneeAvatarView, assignee);
            view.assigneeText.setText(assignee.getLogin());
            view.assigneeArea.setVisibility(VISIBLE);
        } else
            view.assigneeArea.setVisibility(GONE);
    }

    @Override
    protected FilterItemView createView(final View view) {
        return new FilterItemView(view);
    }
}
