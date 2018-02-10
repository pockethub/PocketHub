package com.github.pockethub.android.ui.item.news;

import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.payload.DeletePayload;

public class DeleteEventItem extends NewsItem {

    public DeleteEventItem(AvatarLoader avatarLoader, GitHubEvent dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull NewsItem.ViewHolder viewHolder, int position) {
        super.bind(viewHolder, position);
        viewHolder.icon.setText(OcticonTextView.ICON_DELETE);

        StyledText main = new StyledText();
        boldActor(main, getData());

        DeletePayload payload = (DeletePayload) getData().payload();

        main.append(" deleted ");
        main.append(payload.refType().name().toLowerCase());
        main.append(' ');
        main.append(payload.ref());
        main.append(" at ");

        boldRepo(main, getData());

        viewHolder.event.setText(main);

        viewHolder.details.setVisibility(View.GONE);
    }
}
