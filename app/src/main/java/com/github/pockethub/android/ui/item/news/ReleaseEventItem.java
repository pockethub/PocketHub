package com.github.pockethub.android.ui.item.news;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Release;
import com.meisolsson.githubsdk.model.payload.ReleasePayload;

public class ReleaseEventItem extends NewsItem {

    public ReleaseEventItem(AvatarLoader avatarLoader, GitHubEvent dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull NewsItem.ViewHolder viewHolder, int position) {
        super.bind(viewHolder, position);
        viewHolder.icon.setText(OcticonTextView.ICON_UPLOAD);

        StyledText main = new StyledText();
        boldActor(main, getData());
        main.append(" uploaded a file to ");
        boldRepo(main, getData());
        viewHolder.event.setText(main);

        StyledText details = new StyledText();
        ReleasePayload payload = (ReleasePayload) getData().payload();
        Release download = payload.release();
        if (download != null) {
            appendText(details, download.name());
        }

        if (TextUtils.isEmpty(details)) {
            viewHolder.details.setVisibility(View.GONE);
        } else {
            viewHolder.details.setText(details);
        }

    }
}
