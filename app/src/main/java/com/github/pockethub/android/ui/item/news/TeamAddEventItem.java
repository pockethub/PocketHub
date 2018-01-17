package com.github.pockethub.android.ui.item.news;

import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.Team;
import com.meisolsson.githubsdk.model.payload.TeamAddPayload;

public class TeamAddEventItem extends NewsItem {

    public TeamAddEventItem(AvatarLoader avatarLoader, GitHubEvent dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull NewsItem.ViewHolder viewHolder, int position) {
        super.bind(viewHolder, position);
        viewHolder.icon.setText(OcticonTextView.ICON_ADD_MEMBER);

        StyledText main = new StyledText();
        boldActor(main, getData());

        main.append(" added ");

        TeamAddPayload payload = (TeamAddPayload) getData().payload();
        Repository repo = payload.repository();
        String repoName = repo != null ? repo.name() : null;
        if (repoName != null) {
            main.bold(repoName);
        }

        main.append(" to team");

        Team team = payload.team();
        String teamName = team != null ? team.name() : null;
        if (teamName != null) {
            main.append(' ').bold(teamName);
        }

        viewHolder.event.setText(main);
        viewHolder.details.setVisibility(View.GONE);
    }
}
