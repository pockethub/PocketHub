package com.github.pockethub.android.ui.item.news

import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.MemberPayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class MemberEventItem(avatarLoader: AvatarLoader, override val gitHubEvent: GitHubEvent) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_ADD_MEMBER

        val payload = gitHubEvent.payload() as MemberPayload?

        val main = StyledText()
        boldActor(main, gitHubEvent)
        main.append(" added ")
        main.bold(payload?.member()?.login())
        main.append(" as a collaborator to ")
        boldRepo(main, gitHubEvent)

        holder.tv_event.text = main
        holder.tv_event_details.visibility = View.GONE
    }
}
