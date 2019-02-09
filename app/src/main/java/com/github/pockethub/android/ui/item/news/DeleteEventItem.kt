package com.github.pockethub.android.ui.item.news

import android.view.View
import androidx.core.text.buildSpannedString
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
        holder.tv_event.text = buildSpannedString {
            boldActor(this, gitHubEvent)
            val payload = gitHubEvent.payload() as DeletePayload?
            append(" deleted ${payload?.refType()?.name?.toLowerCase()} ${payload?.ref()} at ")
            boldRepo(this, gitHubEvent)
        }
        holder.tv_event_details.visibility = View.GONE
    }
}
