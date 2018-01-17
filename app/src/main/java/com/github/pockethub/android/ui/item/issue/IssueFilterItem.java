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
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.User;

import java.util.Collection;

import butterknife.BindView;

public class IssueFilterItem extends BaseDataItem<IssueFilter, IssueFilterItem.ViewHolder> {

    public IssueFilterItem(AvatarLoader avatarLoader, IssueFilter issueFilter) {
        super(avatarLoader, issueFilter, issueFilter.hashCode());
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        getAvatarLoader().bind(holder.avatar, getData().getRepository().owner());

        holder.repoName.setText(InfoUtils.createRepoId(getData().getRepository()));

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
        return R.layout.issues_filter_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    class ViewHolder extends BaseViewHolder {

        @BindView(R.id.iv_avatar) ImageView avatar;
        @BindView(R.id.tv_repo_name) TextView repoName;
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
