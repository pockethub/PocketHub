package com.github.pockethub.android.ui.item.news

import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.TeamAddPayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class TeamAddEventItem(
        avatarLoader: AvatarLoader,
        gitHubEvent: GitHubEvent
) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_ADD_MEMBER
        holder.tv_event.text = buildSpannedString {
            boldActor(this, gitHubEvent)
            append(" added ")

            val payload = gitHubEvent.payload() as TeamAddPayload?
            val repo = payload?.repository()
            val repoName = repo?.name()
            if (repoName != null) {
                bold {
                    append(repoName)
                }
            }

            append(" to team")

            val team = payload?.team()
            val teamName = team?.name()
            if (teamName != null) {
                append(' ')
                bold {
                    append(teamName)
                }
            }
        }
        holder.tv_event_details.visibility = View.GONE
    }
}
