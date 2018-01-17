package com.github.pockethub.android.ui.item.issue;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueFilter;
import com.github.pockethub.android.ui.issue.LabelDrawableSpan;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.User;

import java.util.Collection;

import butterknife.BindView;

public class IssueFilterHeaderItem
        extends BaseDataItem<IssueFilter, IssueFilterHeaderItem.ViewHolder> {

    public IssueFilterHeaderItem(AvatarLoader avatarLoader, IssueFilter issueFilter) {
        super(avatarLoader, issueFilter, issueFilter.hashCode());
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        if (getData().isOpen()) {
            holder.filterState.setText(R.string.open_issues);
        } else {
            holder.filterState.setText(R.string.closed_issues);
        }

        Collection<Label> labels = getData().getLabels();
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(holder.filterLabels, labels);
            holder.filterLabels.setVisibility(View.VISIBLE);
        } else {
            holder.filterLabels.setVisibility(View.GONE);
        }

        Milestone milestone = getData().getMilestone();
        if (milestone != null) {
            holder.filterMilestone.setText(milestone.title());
            holder.filterMilestone.setVisibility(View.VISIBLE);
        } else {
            holder.filterMilestone.setVisibility(View.GONE);
        }

        User assignee = getData().getAssignee();
        if (assignee != null) {
            getAvatarLoader().bind(holder.assigneeAvatar, assignee);
            holder.filterAssignee.setText(assignee.login());
            holder.filterAssignee.setVisibility(View.VISIBLE);
        } else {
            holder.assignee.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.issues_filter_header;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    class ViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_filter_state) TextView filterState;
        @BindView(R.id.tv_filter_labels) TextView filterLabels;
        @BindView(R.id.tv_filter_milestone) TextView filterMilestone;
        @BindView(R.id.ll_assignee) LinearLayout assignee;
        @BindView(R.id.tv_filter_assignee) TextView filterAssignee;
        @BindView(R.id.iv_assignee_avatar) ImageView assigneeAvatar;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }

}
