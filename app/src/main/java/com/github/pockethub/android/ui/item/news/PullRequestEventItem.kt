package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.PullRequestPayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class PullRequestEventItem(
        avatarLoader: AvatarLoader,
        gitHubEvent: GitHubEvent
) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_PULL_REQUEST

        val payload = gitHubEvent.payload() as PullRequestPayload
        val action = payload.action()

        holder.tv_event.text = buildSpannedString {
            boldActor(this, gitHubEvent)
            if (PullRequestPayload.Action.Synchronized == action) {
                append("updated")
            }
            append(" ${action?.name?.toLowerCase()} ")
            bold {
                append("pull request " + payload.number())
            }
            append(" on ")
            boldRepo(this, gitHubEvent)
        }

        val details = buildSpannedString {
            if (PullRequestPayload.Action.Opened == action
                    || PullRequestPayload.Action.Closed == action
            ) {
                val request = payload.pullRequest()
                if (request != null) {
                    val title: String? = request.title()
                    if (!TextUtils.isEmpty(title)) {
                        append(title)
                    }
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
