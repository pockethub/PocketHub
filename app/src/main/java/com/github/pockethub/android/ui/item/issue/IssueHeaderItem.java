package com.github.pockethub.android.ui.item.issue;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueUtils;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.issue.LabelDrawableSpan;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.IssueState;
import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.User;

import java.util.Date;
import java.util.List;

import butterknife.BindView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_COMMIT;

public class IssueHeaderItem extends BaseDataItem<Issue, IssueHeaderItem.ViewHolder> {

    private final HttpImageGetter imageGetter;
    private final Context context;
    private final OnIssueHeaderActionListener actionListener;

    public IssueHeaderItem(AvatarLoader avatarLoader, HttpImageGetter imageGetter, Context context,
                           OnIssueHeaderActionListener actionListener, Issue dataItem) {
        super(avatarLoader, dataItem, dataItem.id());
        this.imageGetter = imageGetter;
        this.context = context;
        this.actionListener = actionListener;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        Issue issue = getData();
        holder.title.setText(issue.title());

        String body = issue.bodyHtml();
        if (!TextUtils.isEmpty(body)) {
            imageGetter.bind(holder.bodyText, body, issue.id());
        } else {
            holder.bodyText.setText(R.string.no_description_given);
        }

        holder.author.setText(issue.user().login());

        StyledText created = new StyledText();
        created.append(context.getString(R.string.prefix_opened));
        created.append(issue.createdAt());
        holder.created.setText(created);

        getAvatarLoader().bind(holder.avatar, issue.user());

        if (IssueUtils.isPullRequest(issue) && issue.pullRequest().commits() != null
                && issue.pullRequest().commits() > 0) {
            holder.commitsView.setVisibility(VISIBLE);

            holder.commitIcon.setText(ICON_COMMIT);

            String commits = context.getString(R.string.pull_request_commits,
                    issue.pullRequest().commits());
            holder.pullRequestCommits.setText(commits);
        } else {
            holder.commitsView.setVisibility(GONE);
        }

        boolean open = IssueState.open.equals(issue.state());
        if (!open) {
            StyledText text = new StyledText();
            text.bold(context.getString(R.string.closed));
            Date closedAt = issue.closedAt();
            if (closedAt != null) {
                text.append(' ').append(closedAt);
            }
            holder.state.setText(text);
            holder.state.setVisibility(VISIBLE);
        } else {
            holder.state.setVisibility(GONE);
        }

        User assignee = issue.assignee();
        if (assignee != null) {
            StyledText name = new StyledText();
            name.bold(assignee.login());
            name.append(' ').append(context.getString(R.string.assigned));
            holder.assigneeName.setText(name);
            holder.assigneeAvatar.setVisibility(VISIBLE);
            getAvatarLoader().bind(holder.assigneeAvatar, assignee);
        } else {
            holder.assigneeAvatar.setVisibility(GONE);
            holder.assigneeName.setText(R.string.unassigned);
        }

        List<Label> labels = issue.labels();
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(holder.labels, labels);
            holder.labels.setVisibility(VISIBLE);
        } else {
            holder.labels.setVisibility(GONE);
        }

        if (issue.milestone() != null) {
            Milestone milestone = issue.milestone();
            StyledText milestoneLabel = new StyledText();
            milestoneLabel.append(context.getString(R.string.milestone_prefix));
            milestoneLabel.append(' ');
            milestoneLabel.bold(milestone.title());
            holder.milestoneText.setText(milestoneLabel);
            float closed = milestone.closedIssues();
            float total = closed + milestone.openIssues();
            if (total > 0) {
                ((LinearLayout.LayoutParams) holder.milestoneProgressArea.getLayoutParams())
                        .weight = closed / total;

                holder.milestoneProgressArea.setVisibility(VISIBLE);
            } else {
                holder.milestoneProgressArea.setVisibility(GONE);
            }
            holder.milestoneArea.setVisibility(VISIBLE);
        } else {
            holder.milestoneArea.setVisibility(GONE);
        }

        holder.commitsView.setOnClickListener(v -> actionListener.onCommitsClicked());
        holder.state.setOnClickListener(v -> actionListener.onStateClicked());
        holder.milestoneArea.setOnClickListener(v -> actionListener.onMilestonesClicked());
        holder.assigneeArea.setOnClickListener(v -> actionListener.onAssigneesClicked());
        holder.labels.setOnClickListener(v -> actionListener.onLabelsClicked());

    }

    @Override
    public int getLayout() {
        return R.layout.issue_header;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_state) TextView state;
        @BindView(R.id.tv_issue_title)TextView title;
        @BindView(R.id.tv_issue_author) TextView author;
        @BindView(R.id.tv_issue_creation_date) TextView created;
        @BindView(R.id.iv_avatar) ImageView avatar;
        @BindView(R.id.ll_assignee) LinearLayout assigneeArea;
        @BindView(R.id.tv_assignee_name) TextView assigneeName;
        @BindView(R.id.iv_assignee_avatar) ImageView assigneeAvatar;
        @BindView(R.id.tv_labels) TextView labels;
        @BindView(R.id.ll_issue_commits) LinearLayout commitsView;
        @BindView(R.id.tv_commit_icon) TextView commitIcon;
        @BindView(R.id.tv_pull_request_commits) TextView pullRequestCommits;
        @BindView(R.id.ll_milestone)LinearLayout milestoneArea;
        @BindView(R.id.tv_milestone) TextView milestoneText;
        @BindView(R.id.v_closed) View milestoneProgressArea;
        @BindView(R.id.tv_issue_body) TextView bodyText;


        public ViewHolder(@NonNull View rootView) {
            super(rootView);
            bodyText.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public interface OnIssueHeaderActionListener {

        void onCommitsClicked();

        void onStateClicked();

        void onMilestonesClicked();

        void onAssigneesClicked();

        void onLabelsClicked();
    }
}
