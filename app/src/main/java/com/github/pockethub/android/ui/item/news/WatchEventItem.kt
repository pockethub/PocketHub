package com.github.pockethub.android.ui.item.news

import android.view.View
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class WatchEventItem(
        avatarLoader: AvatarLoader,
        gitHubEvent: GitHubEvent
) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_STAR
        holder.tv_event.text = buildSpannedString {
            boldActor(this, gitHubEvent)
            append(" starred ")
            boldRepo(this, gitHubEvent)
        }
        holder.tv_event_details.visibility = View.GONE
    }
}
