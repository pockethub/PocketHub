package com.github.pockethub.android.ui.item.news

import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.DeletePayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class DeleteEventItem(
        avatarLoader: AvatarLoader,
        gitHubEvent: GitHubEvent
) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_DELETE

        val main = StyledText()
        boldActor(main, gitHubEvent)

        val payload = gitHubEvent.payload() as DeletePayload?

        main.append(" deleted ")
        main.append(payload?.refType()?.name?.toLowerCase())
        main.append(' ')
        main.append(payload?.ref())
        main.append(" at ")

        boldRepo(main, gitHubEvent)

        holder.tv_event_icon.text = main
        holder.tv_event_details.visibility = View.GONE
    }
}
