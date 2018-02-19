package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.core.issue.IssueUtils
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubComment
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.IssueCommentPayload

class IssueCommentEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_ISSUE_COMMENT

        val main = StyledText()
        boldActor(main, data)

        main.append(" commented on ")

        val payload = data.payload() as IssueCommentPayload?
        val issue = payload?.issue()
        val numberAsText: String = if (IssueUtils.isPullRequest(issue)) "pull request " + issue?.number()
            else "issue " + issue?.number()
        main.bold(numberAsText)

        main.append(" on ")

        boldRepo(main, data)

        val details = StyledText()
        appendComment(details, payload?.comment())

        if (TextUtils.isEmpty(details)) {
            viewHolder.details.visibility = View.GONE
        } else {
            viewHolder.details.text = details
        }

        viewHolder.event.text = main
    }

    private fun appendComment(details: StyledText, comment: GitHubComment?) {
        appendText(details, comment?.body())
    }
}
