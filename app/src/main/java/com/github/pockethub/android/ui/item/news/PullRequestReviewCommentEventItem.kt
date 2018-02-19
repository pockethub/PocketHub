package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.ReviewComment
import com.meisolsson.githubsdk.model.payload.PullRequestReviewCommentPayload

class PullRequestReviewCommentEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_COMMENT

        val event = data

        val main = StyledText()
        boldActor(main, event)
        main.append(" commented on ")
        boldRepo(main, event)
        viewHolder.event.text = main

        val details = StyledText()
        val payload = event.payload() as PullRequestReviewCommentPayload?
        appendReviewComment(details, payload?.comment())

        if (TextUtils.isEmpty(details)) {
            viewHolder.details.visibility = View.GONE
        } else {
            viewHolder.details.text = details
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
