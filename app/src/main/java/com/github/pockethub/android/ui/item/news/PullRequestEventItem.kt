package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.PullRequestPayload

class PullRequestEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_PULL_REQUEST

        val payload = data.payload() as PullRequestPayload

        val main = StyledText()
        boldActor(main, data)

        val action = payload.action()
        if (PullRequestPayload.Action.Synchronized == action) {
            main.append("updated")
        }
        main.append(' ')
        main.append(action?.name?.toLowerCase())
        main.append(' ')
        main.bold("pull request " + payload.number())
        main.append(" on ")

        boldRepo(main, data)

        viewHolder.event.text = main

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
            viewHolder.details.visibility = View.GONE
        } else {
            viewHolder.details.text = details
        }
    }
}
