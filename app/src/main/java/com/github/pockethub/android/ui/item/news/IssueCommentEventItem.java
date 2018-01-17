package com.github.pockethub.android.ui.item.news;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.github.pockethub.android.core.issue.IssueUtils;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.payload.IssueCommentPayload;

public class IssueCommentEventItem extends NewsItem {

    public IssueCommentEventItem(AvatarLoader avatarLoader, GitHubEvent dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull NewsItem.ViewHolder viewHolder, int position) {
        super.bind(viewHolder, position);
        viewHolder.icon.setText(OcticonTextView.ICON_ISSUE_COMMENT);

        StyledText main = new StyledText();
        boldActor(main, getData());

        main.append(" commented on ");

        IssueCommentPayload payload = (IssueCommentPayload) getData().payload();
        Issue issue = payload.issue();
        String number;
        if (IssueUtils.isPullRequest(issue)) {
            number = "pull request " + issue.number();
        } else {
            number = "issue " + issue.number();
        }
        main.bold(number);

        main.append(" on ");

        boldRepo(main, getData());

        StyledText details = new StyledText();
        appendComment(details, payload.comment());

        if (TextUtils.isEmpty(details)) {
            viewHolder.details.setVisibility(View.GONE);
        } else {
            viewHolder.details.setText(details);
        }

        viewHolder.event.setText(main);
    }

    private void appendComment(StyledText details, GitHubComment comment) {
        if (comment != null) {
            appendText(details, comment.body());
        }
    }
}
