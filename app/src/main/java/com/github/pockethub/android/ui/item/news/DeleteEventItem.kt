package com.github.pockethub.android.ui.item.news

import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.DeletePayload

class DeleteEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_DELETE

        val main = StyledText()
        boldActor(main, data)

        val payload = data.payload() as DeletePayload?

        main.append(" deleted ")
        main.append(payload?.refType()?.name?.toLowerCase())
        main.append(' ')
        main.append(payload?.ref())
        main.append(" at ")

        boldRepo(main, data)

        viewHolder.event.text = main
        viewHolder.details.visibility = View.GONE
    }
}
