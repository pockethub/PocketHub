package com.github.pockethub.android.ui.item.news;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.PullRequest;
import com.meisolsson.githubsdk.model.payload.PullRequestPayload;

public class PullRequestEventItem extends NewsItem {

    public PullRequestEventItem(AvatarLoader avatarLoader, GitHubEvent dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull NewsItem.ViewHolder viewHolder, int position) {
        super.bind(viewHolder, position);
        viewHolder.icon.setText(OcticonTextView.ICON_PULL_REQUEST);

        PullRequestPayload payload = (PullRequestPayload) getData().payload();

        StyledText main = new StyledText();
        boldActor(main, getData());

        PullRequestPayload.Action action = payload.action();
        if (PullRequestPayload.Action.Synchronized.equals(action)) {
            main.append("updated");
        }
        main.append(' ');
        main.append(action.name().toLowerCase());
        main.append(' ');
        main.bold("pull request " + payload.number());
        main.append(" on ");

        boldRepo(main, getData());

        viewHolder.event.setText(main);

        StyledText details = new StyledText();
        if (PullRequestPayload.Action.Opened.equals(action) ||
                PullRequestPayload.Action.Closed.equals(action)) {
            PullRequest request = payload.pullRequest();
            if (request != null) {
                String title = request.title();
                if (!TextUtils.isEmpty(title)) {
                    details.append(title);
                }
            }
        }

        if (TextUtils.isEmpty(details)) {
            viewHolder.details.setVisibility(View.GONE);
        } else {
            viewHolder.details.setText(details);
        }
    }
}
