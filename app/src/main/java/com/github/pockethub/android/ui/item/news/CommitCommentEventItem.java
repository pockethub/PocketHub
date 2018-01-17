package com.github.pockethub.android.ui.item.news;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.git.GitComment;
import com.meisolsson.githubsdk.model.payload.CommitCommentPayload;

public class CommitCommentEventItem extends NewsItem {

    public CommitCommentEventItem(AvatarLoader avatarLoader, GitHubEvent dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull NewsItem.ViewHolder viewHolder, int position) {
        super.bind(viewHolder, position);
        viewHolder.icon.setText(OcticonTextView.ICON_COMMENT);

        StyledText main = new StyledText();
        boldActor(main, getData());
        main.append(" commented on ");
        boldRepo(main, getData());
        viewHolder.event.setText(main);

        StyledText details = new StyledText();
        CommitCommentPayload payload = (CommitCommentPayload) getData().payload();

        appendCommitComment(details, payload.comment());
        if (TextUtils.isEmpty(details)) {
            viewHolder.details.setVisibility(View.GONE);
        } else {
            viewHolder.details.setText(details);
        }
    }

    private void appendCommitComment(StyledText details, GitComment comment) {
        if (comment == null) {
            return;
        }

        String id = comment.commitId();
        if (!TextUtils.isEmpty(id)) {
            if (id.length() > 10) {
                id = id.substring(0, 10);
            }
            appendText(details, "Comment in");
            details.append(' ');
            details.monospace(id);
            details.append(':').append('\n');
        }
        appendText(details, comment.body());
    }
}
