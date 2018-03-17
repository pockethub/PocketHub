package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.PullRequestPayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class PullRequestEventItem(avatarLoader: AvatarLoader, override val gitHubEvent: GitHubEvent) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_PULL_REQUEST

        val payload = gitHubEvent.payload() as PullRequestPayload

        val main = StyledText()
        boldActor(main, gitHubEvent)

        val action = payload.action()
        if (PullRequestPayload.Action.Synchronized == action) {
            main.append("updated")
        }
        main.append(' ')
        main.append(action?.name?.toLowerCase())
        main.append(' ')
        main.bold("pull request " + payload.number())
        main.append(" on ")

        boldRepo(main, gitHubEvent)

        holder.tv_event.text = main

        val details = StyledText()
        if (PullRequestPayload.Action.Opened == action || PullRequestPayload.Action.Closed == action) {
            val request = payload.pullRequest()
            if (request != null) {
                val title: String? = request.title()
                if (!TextUtils.isEmpty(title)) {
                    details.append(title)
                }
            }
        }

        if (TextUtils.isEmpty(details)) {
            holder.tv_event_details.visibility = View.GONE
        } else {
            holder.tv_event_details.text = details
        }
    }
}
