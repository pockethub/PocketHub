package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.ReviewComment
import com.meisolsson.githubsdk.model.payload.PullRequestReviewCommentPayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class PullRequestReviewCommentEventItem(avatarLoader: AvatarLoader, override val gitHubEvent: GitHubEvent) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_COMMENT

        val main = StyledText()
        boldActor(main, gitHubEvent)
        main.append(" commented on ")
        boldRepo(main, gitHubEvent)
        holder.tv_event.text = main

        val details = StyledText()
        val payload = gitHubEvent.payload() as PullRequestReviewCommentPayload?
        appendReviewComment(details, payload?.comment())

        if (TextUtils.isEmpty(details)) {
            holder.tv_event_details.visibility = View.GONE
        } else {
            holder.tv_event_details.text = details
        }
    }

    private fun appendReviewComment(details: StyledText, comment: ReviewComment?) {
        if (comment == null) {
            return
        }

        var id: String? = comment.commitId()
        if (!TextUtils.isEmpty(id)) {
            if (id!!.length > 10) {
                id = id.substring(0, 10)
            }
            appendText(details, "Comment in")
            details.append(' ')
            details.monospace(id)
            details.append(':').append('\n')
        }
        appendText(details, comment.body())
    }
}
