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
package com.github.mobile.issue;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.R.string;
import com.github.mobile.util.AvatarUtils;
import com.github.mobile.util.ServiceUtils;
import com.madgag.android.listviews.ViewHolder;

import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.User;

/**
 * Holder for a issue minus the comments
 */
public class CreateIssueHeaderViewHolder implements ViewHolder<Issue> {

    private final AvatarUtils avatarHelper;

    private final Resources resources;

    private final TextView assigneeText;

    private final ImageView assigneeAvatar;

    private final View labelsArea;

    private final TextView milestoneText;

    /**
     * Create issue header view holder
     *
     * @param view
     * @param avatarHelper
     * @param resources
     */
    public CreateIssueHeaderViewHolder(final View view, final AvatarUtils avatarHelper, final Resources resources) {
        this.avatarHelper = avatarHelper;
        this.resources = resources;
        assigneeText = (TextView) view.findViewById(id.tv_assignee_name);
        assigneeAvatar = (ImageView) view.findViewById(id.iv_assignee_gravatar);
        labelsArea = view.findViewById(id.v_labels);
        milestoneText = (TextView) view.findViewById(id.tv_milestone);
    }

    public void updateViewFor(Issue issue) {
        User assignee = issue.getAssignee();
        if (assignee != null) {
            assigneeText.setText(assignee.getLogin());
            assigneeAvatar.setVisibility(VISIBLE);
            avatarHelper.bind(assigneeAvatar, assignee);
        } else {
            assigneeAvatar.setVisibility(GONE);
            assigneeText.setText("Unassigned");
        }

        List<Label> labels = issue.getLabels();
        if (labels != null && !labels.isEmpty()) {
            labelsArea.setVisibility(VISIBLE);
            LabelsDrawable drawable = new LabelsDrawable(labelsArea.getPaddingLeft(), assigneeText.getTextSize(),
                    ServiceUtils.getDisplayWidth(labelsArea) - labelsArea.getPaddingLeft()
                            - labelsArea.getPaddingRight(), issue.getLabels());
            drawable.getPaint().setColor(resources.getColor(android.R.color.transparent));
            labelsArea.setBackgroundDrawable(drawable);
            LayoutParams params = new LayoutParams(drawable.getBounds().width(), drawable.getBounds().height());
            labelsArea.setLayoutParams(params);
        } else
            labelsArea.setVisibility(GONE);

        if (issue.getMilestone() != null)
            milestoneText.setText(issue.getMilestone().getTitle());
        else
            milestoneText.setText(milestoneText.getContext().getString(string.no_milestone));
    }
}
