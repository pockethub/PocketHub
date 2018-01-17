package com.github.pockethub.android.ui.item.news;

import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.payload.GistPayload;

public class GistEventItem extends NewsItem {

    public GistEventItem(AvatarLoader avatarLoader, GitHubEvent dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull NewsItem.ViewHolder viewHolder, int position) {
        super.bind(viewHolder, position);
        viewHolder.icon.setText(OcticonTextView.ICON_GIST);

        StyledText main = new StyledText();
        boldActor(main, getData());

        GistPayload payload = (GistPayload) getData().payload();

        main.append(' ');
        String action = payload.action();
        if ("create".equals(action)) {
            main.append("created");
        } else if ("update".equals(action)) {
            main.append("updated");
        } else {
            main.append(action);
        }
        main.append(" Gist ");
        main.append(payload.gist().id());

        viewHolder.event.setText(main);
        viewHolder.details.setVisibility(View.GONE);
    }
}
