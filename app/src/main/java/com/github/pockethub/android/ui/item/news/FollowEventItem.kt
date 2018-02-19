package com.github.pockethub.android.ui.item.news

import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.FollowPayload

class FollowEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_FOLLOW

        val main = StyledText()
        boldActor(main, data)
        main.append(" started following ")
        boldUser(main, (data.payload() as FollowPayload?)?.target())

        viewHolder.event.text = main
        viewHolder.details.visibility = View.GONE
    }
}
