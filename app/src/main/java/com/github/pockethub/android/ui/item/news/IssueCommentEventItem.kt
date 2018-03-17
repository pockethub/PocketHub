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
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class IssueCommentEventItem(avatarLoader: AvatarLoader, override val gitHubEvent: GitHubEvent) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_ISSUE_COMMENT

        val main = StyledText()
        boldActor(main, gitHubEvent)

        main.append(" commented on ")

        val payload = gitHubEvent.payload() as IssueCommentPayload?
        val issue = payload?.issue()
        val numberAsText: String = if (IssueUtils.isPullRequest(issue)) "pull request " + issue?.number()
        else "issue " + issue?.number()
        main.bold(numberAsText)

        main.append(" on ")

        boldRepo(main, gitHubEvent)

        val details = StyledText()
        appendComment(details, payload?.comment())

        if (TextUtils.isEmpty(details)) {
            holder.tv_event_details.visibility = View.GONE
        } else {
            holder.tv_event_details.text = details
        }

        holder.tv_event.text = main
    }

    private fun appendComment(details: StyledText, comment: GitHubComment?) {
        appendText(details, comment?.body())
    }
}
