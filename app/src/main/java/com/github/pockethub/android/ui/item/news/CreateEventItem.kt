package com.github.pockethub.android.ui.item.news

import android.view.View
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.CreatePayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class CreateEventItem(
        avatarLoader: AvatarLoader,
        gitHubEvent: GitHubEvent
) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_CREATE
        holder.tv_event.text = buildSpannedString {
            boldActor(this, gitHubEvent)
            val payload = gitHubEvent.payload() as CreatePayload?

            val refType: String? = payload?.refType()?.name?.toLowerCase()
            append(" created $refType ")

            if ("repository" != refType) {
                append("${payload?.ref()} at ")
                boldRepo(this, gitHubEvent)
            } else {
                boldRepoName(this, gitHubEvent)
            }
        }
        holder.tv_event_details.visibility = View.GONE
    }
}
