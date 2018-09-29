package com.github.pockethub.android.ui.item.news

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.util.android.text.monospace
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.git.GitComment
import com.meisolsson.githubsdk.model.payload.CommitCommentPayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class CommitCommentEventItem(
        avatarLoader: AvatarLoader,
        gitHubEvent: GitHubEvent
) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_COMMENT

        holder.tv_event.text = buildSpannedString {
            boldActor(this, gitHubEvent)
            append(" commented on ")
            boldRepo(this, gitHubEvent)
        }

        val details = buildSpannedString {
            val payload = gitHubEvent.payload() as CommitCommentPayload?
            appendCommitComment(this, payload?.comment())
        }

        if (TextUtils.isEmpty(details)) {
            holder.tv_event_details.visibility = View.GONE
        } else {
            holder.tv_event_details.text = details
        }
    }

    private fun appendCommitComment(details: SpannableStringBuilder, comment: GitComment?) {
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
            details.monospace {
                append(id)
            }
            details.append(":\n")
        }
        appendText(details, comment.body())
    }
}
