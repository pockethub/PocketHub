package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.IssuesPayload

class IssuesEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        val payload = data.payload() as IssuesPayload?
        val action = payload?.action()

        if (action != null) {
            when (action) {
                IssuesPayload.Action.Opened -> viewHolder.icon.text = OcticonTextView.ICON_ISSUE_OPEN
                IssuesPayload.Action.Reopened -> viewHolder.icon.text = OcticonTextView.ICON_ISSUE_REOPEN
                IssuesPayload.Action.Closed -> viewHolder.icon.text = OcticonTextView.ICON_ISSUE_CLOSE
                else -> viewHolder.icon.visibility = View.GONE
            }
        }

        val main = StyledText()
        boldActor(main, data)

        val issue = payload?.issue()
        main.append(' ')
        main.append(action?.name?.toLowerCase())
        main.append(' ')
        main.bold("issue " + issue?.number())
        main.append(" on ")

        boldRepo(main, data)

        val details = StyledText()
        appendText(details, issue?.title())

        if (TextUtils.isEmpty(details)) {
            viewHolder.details.visibility = View.GONE
        } else {
            viewHolder.details.text = details
        }

        viewHolder.event.text = main
    }
}
