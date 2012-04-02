package com.github.mobile.android.issue;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.ServiceHelper;
import com.madgag.android.listviews.ViewHolder;

import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.User;

/**
 * Holder for a issue minus the comments
 */
public class CreateIssueHeaderViewHolder implements ViewHolder<Issue> {

    private final AvatarHelper avatarHelper;

    private final Resources resources;

    private final TextView assigneeText;

    private final ImageView assigneeAvatar;

    private final LinearLayout labelsArea;

    private final TextView milestoneText;

    /**
     * Create issue header view holder
     *
     * @param view
     * @param avatarHelper
     * @param resources
     */
    public CreateIssueHeaderViewHolder(final View view, final AvatarHelper avatarHelper, final Resources resources) {
        this.avatarHelper = avatarHelper;
        this.resources = resources;
        assigneeText = (TextView) view.findViewById(id.tv_assignee_name);
        assigneeAvatar = (ImageView) view.findViewById(id.iv_assignee_gravatar);
        labelsArea = (LinearLayout) view.findViewById(id.ll_labels);
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
            LabelsDrawable drawable = new LabelsDrawable(assigneeText.getTextSize(),
                    ServiceHelper.getDisplayWidth(labelsArea), issue.getLabels());
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
