package com.github.pockethub.android.ui.item.news

import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.CreatePayload


class CreateEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) :
    NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_CREATE

        val main = StyledText()
        boldActor(main, data)
        val payload = data.payload() as CreatePayload?

        main.append(" created ")
        val refType: String? = payload?.refType()?.name?.toLowerCase()
        main.append(refType)
        main.append(' ')

        if ("repository" != refType) {
            main.append(payload?.ref())
            main.append(" at ")
            boldRepo(main, data)
        } else {
            boldRepoName(main, data)
        }

        viewHolder.event.text = main
        viewHolder.details.visibility = View.GONE
    }
}