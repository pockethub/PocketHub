package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.git.GitComment
import com.meisolsson.githubsdk.model.payload.CommitCommentPayload

class CommitCommentEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_COMMENT

        val main = StyledText()
        boldActor(main, data)
        main.append(" commented on ")
        boldRepo(main, data)
        viewHolder.event.text = main

        val details = StyledText()
        val payload = data.payload() as CommitCommentPayload?

        appendCommitComment(details, payload?.comment())
        if (TextUtils.isEmpty(details)) {
            viewHolder.details.visibility = View.GONE
        } else {
            viewHolder.details.text = details
        }
    }

    private fun appendCommitComment(details: StyledText, comment: GitComment?) {
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
