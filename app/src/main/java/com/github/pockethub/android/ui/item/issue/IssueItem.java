package com.github.pockethub.android.ui.item.issue;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueUtils;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.ButterKnifeUtils;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.IssueState;
import com.meisolsson.githubsdk.model.Label;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class IssueItem extends BaseDataItem<Issue, IssueItem.ViewHolder> {

    private final boolean showLabels;

    public IssueItem(AvatarLoader avatarLoader, Issue dataItem) {
        this(avatarLoader, dataItem, true);
    }

    public IssueItem(AvatarLoader avatarLoader, Issue dataItem, boolean showLabels) {
        super(avatarLoader, dataItem, dataItem.id());
        this.showLabels = showLabels;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        List<Label> labels = getData().labels();
        if (showLabels && labels != null && !labels.isEmpty()) {
            ButterKnife.apply(holder.labels, LABEL_SETTER, labels);
        } else {
            ButterKnife.apply(holder.labels, ButterKnifeUtils.GONE);
        }

        StyledText numberText = new StyledText();
        numberText.append(String.valueOf(getData().number()));
        if (IssueState.closed.equals(getData().state())) {
            numberText.strikethroughAll();
        }

        holder.number.setText(numberText);

        getAvatarLoader().bind(holder.avatar, getData().user());

        if (IssueUtils.isPullRequest(getData())) {
            holder.pullRequest.setVisibility(View.VISIBLE);
        } else {
            holder.pullRequest.setVisibility(View.GONE);
        }

        holder.title.setText(getData().title());
        holder.comments.setText(String.valueOf(getData().comments()));

        StyledText reporterText = new StyledText();
        reporterText.bold(getData().user().login());
        reporterText.append(' ');
        reporterText.append(getData().createdAt());
        holder.creation.setText(reporterText);
    }

    @Override
    public int getLayout() {
        return R.layout.repo_issue_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    private static final ButterKnife.Setter<View, List<Label>> LABEL_SETTER = (view, labels, i) -> {
        if (i >= 0 && i < labels.size()) {
            Label label = labels.get(i);
            if (!TextUtils.isEmpty(label.color())) {
                view.setBackgroundColor(Color.parseColor('#' + label.color()));
                view.setVisibility(View.VISIBLE);
                return;
            }
        }

        view.setVisibility(View.GONE);
    };

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_issue_number) TextView number;
        @BindView(R.id.tv_issue_title) TextView title;
        @BindView(R.id.iv_avatar) ImageView avatar;
        @BindView(R.id.tv_issue_creation) TextView creation;
        @BindView(R.id.tv_issue_comments) TextView comments;
        @BindView(R.id.tv_pull_request_icon) TextView pullRequest;

        @BindViews({R.id.v_label0, R.id.v_label1, R.id.v_label2, R.id.v_label3,
                R.id.v_label4, R.id.v_label5, R.id.v_label6, R.id.v_label7})
        List<View> labels;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
