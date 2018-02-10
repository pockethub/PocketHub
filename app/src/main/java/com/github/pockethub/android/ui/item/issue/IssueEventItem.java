package com.github.pockethub.android.ui.item.issue;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.TimeUtils;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.IssueEvent;
import com.meisolsson.githubsdk.model.IssueEventType;

import butterknife.BindView;

public class IssueEventItem extends BaseDataItem<IssueEvent, IssueEventItem.ViewHolder> {

    private final Context context;
    private final Issue issue;

    public IssueEventItem(AvatarLoader avatarLoader, Context context, Issue issue, IssueEvent dataItem) {
        super(avatarLoader, dataItem, dataItem.id());
        this.context = context;
        this.issue = issue;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        IssueEvent event = getData();
        String message = String.format("<b>%s</b> %s", event.actor().login(), event.event());
        getAvatarLoader().bind(holder.avatar, event.actor());

        IssueEventType eventType = event.event();

        switch (eventType) {
            case Assigned:
            case Unassigned:
                holder.icon.setText(OcticonTextView.ICON_PERSON);
                holder.icon.setTextColor(
                        context.getResources().getColor(R.color.text_description));
                break;
            case Labeled:
            case Unlabeled:
                holder.icon.setText(OcticonTextView.ICON_TAG);
                holder.icon.setTextColor(
                        context.getResources().getColor(R.color.text_description));
                break;
            case Referenced:
                holder.icon.setText(OcticonTextView.ICON_BOOKMARK);
                holder.icon.setTextColor(
                        context.getResources().getColor(R.color.text_description));
                break;
            case Milestoned:
            case Demilestoned:
                holder.icon.setText(OcticonTextView.ICON_MILESTONE);
                holder.icon.setTextColor(
                        context.getResources().getColor(R.color.text_description));
                break;
            case Closed:
                holder.icon.setText(OcticonTextView.ICON_ISSUE_CLOSE);
                holder.icon.setTextColor(
                        context.getResources().getColor(R.color.issue_event_closed));
                break;
            case Reopened:
                holder.icon.setText(OcticonTextView.ICON_ISSUE_REOPEN);
                holder.icon.setTextColor(
                        context.getResources().getColor(R.color.issue_event_reopened));
                break;
            case Renamed:
                holder.icon.setText(OcticonTextView.ICON_EDIT);
                holder.icon.setTextColor(
                        context.getResources().getColor(R.color.text_description));
                break;
            case Merged:
                message += String.format(" commit <b>%s</b> into <tt>%s</tt> from <tt>%s</tt>", event.commitId().substring(0, 6),
                        issue.pullRequest().base().ref(),
                        issue.pullRequest().head().ref());
                holder.icon.setText(OcticonTextView.ICON_MERGE);
                holder.icon.setTextColor(
                        context.getResources().getColor(R.color.issue_event_merged));
                break;
            case Locked:
                holder.icon.setText(OcticonTextView.ICON_LOCK);
                holder.icon.setTextColor(
                        context.getResources().getColor(R.color.issue_event_lock));
                break;
            case Unlocked:
                holder.icon.setText(OcticonTextView.ICON_KEY);
                holder.icon.setTextColor(
                        context.getResources().getColor(R.color.issue_event_lock));
                break;
        }

        message += " " + TimeUtils.getRelativeTime(event.createdAt());
        holder.event.setText(Html.fromHtml(message));
    }

    @Override
    public int getLayout() {
        return R.layout.comment_event_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {
        
        @BindView(R.id.tv_event_icon) TextView icon;
        @BindView(R.id.tv_event) TextView event;
        @BindView(R.id.iv_avatar) ImageView avatar;
        
        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
