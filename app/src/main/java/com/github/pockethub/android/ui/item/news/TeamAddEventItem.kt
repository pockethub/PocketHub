package com.github.pockethub.android.ui.item.news

import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.TeamAddPayload

class TeamAddEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_ADD_MEMBER

        val main = StyledText()
        boldActor(main, data)

        main.append(" added ")

        val payload = data.payload() as TeamAddPayload?
        val repo = payload?.repository()
        val repoName = repo?.name()

        if (repoName != null) {
            main.bold(repoName)
        }

        main.append(" to team")

        val team = payload?.team()
        val teamName = team?.name()
        if (teamName != null) {
            main.append(' ').bold(teamName)
        }

        viewHolder.event.text = main
        viewHolder.details.visibility = View.GONE
    }
}
