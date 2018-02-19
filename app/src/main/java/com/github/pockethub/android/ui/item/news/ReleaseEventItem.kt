package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.Release
import com.meisolsson.githubsdk.model.payload.ReleasePayload

class ReleaseEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_UPLOAD

        val main = StyledText()
        boldActor(main, data)
        main.append(" uploaded a file to ")
        boldRepo(main, data)
        viewHolder.event.text = main

        val details = StyledText()
        val payload = data.payload() as ReleasePayload?
        val download: Release? = payload?.release()
        appendText(details, download?.name())

        if (TextUtils.isEmpty(details)) {
            viewHolder.details.visibility = View.GONE
        } else {
            viewHolder.details.text = details
        }

    }
}
