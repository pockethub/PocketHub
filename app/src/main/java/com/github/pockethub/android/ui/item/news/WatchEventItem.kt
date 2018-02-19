package com.github.pockethub.android.ui.item.news

import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent

class WatchEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_STAR

        val main = StyledText()
        boldActor(main, data)
        main.append(" starred ")
        boldRepo(main, data)

        viewHolder.event.text = main
        viewHolder.details.visibility = View.GONE
    }
}
