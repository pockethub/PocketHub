package com.github.pockethub.android.ui.item.news;

import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.payload.CreatePayload;

public class CreateEventItem extends NewsItem {

    public CreateEventItem(AvatarLoader avatarLoader, GitHubEvent dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull NewsItem.ViewHolder viewHolder, int position) {
        super.bind(viewHolder, position);
        viewHolder.icon.setText(OcticonTextView.ICON_CREATE);

        StyledText main = new StyledText();
        boldActor(main, getData());
        CreatePayload payload = (CreatePayload) getData().payload();

        main.append(" created ");
        String refType = payload.refType().name();
        main.append(refType);
        main.append(' ');

        if (!"Repository".equals(refType)) {
            main.append(payload.ref());
            main.append(" at ");
            boldRepo(main, getData());
        } else {
            boldRepoName(main, getData());
        }

        viewHolder.event.setText(main);
        viewHolder.details.setVisibility(View.GONE);
    }
}
