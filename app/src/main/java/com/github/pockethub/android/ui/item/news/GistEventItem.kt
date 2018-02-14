package com.github.pockethub.android.ui.item.news

import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.GistPayload

class GistEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_GIST

        val main = StyledText()
        boldActor(main, data)

        val payload = data.payload() as GistPayload?

        main.append(' ')
        val action = payload?.action()

        val status: String? = when(action) {
            GistPayload.Action.Created -> "created"
            GistPayload.Action.Updated -> "updated"
            else -> action?.name
        }

        main.append(status)
        main.append(" Gist ")
        main.append(payload?.gist()?.id())

        viewHolder.event.text = main
        viewHolder.details.visibility = View.GONE
    }
}
