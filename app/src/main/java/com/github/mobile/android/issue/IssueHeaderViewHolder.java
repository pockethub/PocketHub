package com.github.mobile.android.issue;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.content.res.Resources;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.HttpImageGetter;
import com.github.mobile.android.util.ServiceHelper;
import com.github.mobile.android.util.Time;
import com.madgag.android.listviews.ViewHolder;

import java.util.Locale;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.User;

/**
 * Holder for a issue minus the comments
 */
public class IssueHeaderViewHolder implements ViewHolder<Issue> {

    private final AvatarHelper avatarHelper;

    private final HttpImageGetter imageGetter;

    private final Resources resources;

    private final TextView titleText;

    private final TextView bodyText;

    private final TextView createdText;

    private final ImageView creatorAvatar;

    private final TextView assigneeText;

    private final ImageView assigneeAvatar;

    private final LinearLayout labelsArea;

    private final TextView milestoneText;

    private final TextView stateText;

    /**
     * Create issue header view holder
     *
     * @param view
     * @param avatarHelper
     */
    public IssueHeaderViewHolder(final View view, final AvatarHelper avatarHelper) {
        this.avatarHelper = avatarHelper;
        this.resources = view.getResources();
        this.imageGetter = new HttpImageGetter(view.getContext());

        titleText = (TextView) view.findViewById(id.tv_issue_title);
        createdText = (TextView) view.findViewById(id.tv_issue_creation);
        creatorAvatar = (ImageView) view.findViewById(id.iv_gravatar);
        assigneeText = (TextView) view.findViewById(id.tv_assignee_name);
        assigneeAvatar = (ImageView) view.findViewById(id.iv_assignee_gravatar);
        labelsArea = (LinearLayout) view.findViewById(id.ll_labels);
        milestoneText = (TextView) view.findViewById(id.tv_milestone);
        stateText = (TextView) view.findViewById(id.tv_state);
        bodyText = (TextView) view.findViewById(id.tv_issue_body);
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void updateViewFor(Issue issue) {
        titleText.setText(issue.getTitle());
        imageGetter.bind(bodyText, issue.getBodyHtml(), issue.getId());

        String reported = "<b>" + issue.getUser().getLogin() + "</b> opened "
                + Time.relativeTimeFor(issue.getCreatedAt());

        createdText.setText(Html.fromHtml(reported));
        avatarHelper.bind(creatorAvatar, issue.getUser());

        User assignee = issue.getAssignee();
        if (assignee != null) {
            assigneeText.setText(assignee.getLogin());
            assigneeAvatar.setVisibility(VISIBLE);
            avatarHelper.bind(assigneeAvatar, assignee);
        } else {
            assigneeAvatar.setVisibility(GONE);
            assigneeText.setText(assigneeText.getContext().getString(string.unassigned));
        }

        if (!issue.getLabels().isEmpty()) {
            labelsArea.setVisibility(VISIBLE);
            LabelsDrawable drawable = new LabelsDrawable(createdText.getTextSize(),
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

        String state = issue.getState();
        if (state != null && state.length() > 0)
            state = state.substring(0, 1).toUpperCase(Locale.US) + state.substring(1);
        else
            state = "";
        stateText.setText(state);
    }
}
