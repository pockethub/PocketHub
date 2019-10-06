package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.ui.issue.IssuesViewActivity
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ConvertUtils
import com.github.pockethub.android.util.android.text.clickable
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.IssuesPayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

class IssuesEventItem(
        avatarLoader: AvatarLoader,
        gitHubEvent: GitHubEvent
) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        val payload = gitHubEvent.payload() as IssuesPayload?
        val action = payload?.action()

        if (action != null) {
            holder.tv_event_icon.visibility = View.VISIBLE
            when (action) {
                IssuesPayload.Action.Opened ->
                    holder.tv_event_icon.text = OcticonTextView.ICON_ISSUE_OPEN
                IssuesPayload.Action.Reopened ->
                    holder.tv_event_icon.text = OcticonTextView.ICON_ISSUE_REOPEN
                IssuesPayload.Action.Closed ->
                    holder.tv_event_icon.text = OcticonTextView.ICON_ISSUE_CLOSE
                else -> holder.tv_event_icon.visibility = View.GONE
            }
        }

        val issue = payload?.issue()

        val details = buildSpannedString {
            appendText(this, issue?.title())
        }

        if (TextUtils.isEmpty(details)) {
            holder.tv_event_details.visibility = View.GONE
        } else {
            holder.tv_event_details.visibility = View.VISIBLE
            holder.tv_event_details.text = details
        }

        holder.tv_event.text = buildSpannedString {
            val context = holder.root.context
            boldActor(context, this, gitHubEvent)
            append(" ${action?.name?.toLowerCase()} ")
            bold {
                clickable(onClick = {
                    val repository = ConvertUtils.eventRepoToRepo(gitHubEvent.repo())
                    context.startActivity(IssuesViewActivity.createIntent(issue!!, repository))
                }) {
                    append("issue " + issue?.number())
                }
            }
            append(" on ")
            boldRepo(context, this, gitHubEvent)
        }
    }
}
