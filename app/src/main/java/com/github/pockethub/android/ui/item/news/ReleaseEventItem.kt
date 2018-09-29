package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.Release
import com.meisolsson.githubsdk.model.payload.ReleasePayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class ReleaseEventItem(
        avatarLoader: AvatarLoader,
        gitHubEvent: GitHubEvent
) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_UPLOAD
        holder.tv_event.text = buildSpannedString {
            boldActor(this, gitHubEvent)
            append(" uploaded a file to ")
            boldRepo(this, gitHubEvent)
        }

        val details = buildSpannedString {
            val payload = gitHubEvent.payload() as ReleasePayload?
            val download: Release? = payload?.release()
            appendText(this, download?.name())
        }
        if (TextUtils.isEmpty(details)) {
            holder.tv_event_details.visibility = View.GONE
        } else {
            holder.tv_event_details.text = details
        }
    }
}
