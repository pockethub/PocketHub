package com.github.pockethub.android.ui.item.news

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.core.issue.IssueUtils
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubComment
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.IssueCommentPayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class IssueCommentEventItem(
        avatarLoader: AvatarLoader,
        gitHubEvent: GitHubEvent
) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_ISSUE_COMMENT

        val payload = gitHubEvent.payload() as IssueCommentPayload?

        val details = buildSpannedString {
            appendComment(this, payload?.comment())
        }

        if (TextUtils.isEmpty(details)) {
            holder.tv_event_details.visibility = View.GONE
        } else {
            holder.tv_event_details.text = details
        }

        holder.tv_event.text = buildSpannedString {
            boldActor(this, gitHubEvent)
            append(" commented on ")
            bold {
                val issue = payload?.issue()
                append("${if (IssueUtils.isPullRequest(issue)) {
                    "pull request"
                } else {
                    "issue"
                }} ${issue?.number()}")
            }
            append(" on ")
            boldRepo(this, gitHubEvent)
        }
    }

    private fun appendComment(details: SpannableStringBuilder, comment: GitHubComment?) {
        appendText(details, comment?.body())
    }
}
