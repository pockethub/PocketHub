package com.github.pockethub.android.ui.item.issue

import android.content.Context
import android.text.Html
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.IssueEvent
import com.meisolsson.githubsdk.model.IssueEventType.*
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.comment_event.*
import kotlinx.android.synthetic.main.comment_event.view.*

class IssueEventItem(
        private val avatarLoader: AvatarLoader,
        private val context: Context,
        private val issue: Issue,
        val issueEvent: IssueEvent
) : Item(issueEvent.id()!!.toLong()) {

    override fun getLayout(): Int = R.layout.comment_event_item

    override fun bind(holder: ViewHolder, position: Int) {
        var message = String.format(
                "<b>%s</b> %s",
                issueEvent.actor()!!.login(),
                issueEvent.event()
        )
        avatarLoader.bind(holder.itemView.iv_avatar, issueEvent.actor())

        val eventType = issueEvent.event()

        when (eventType) {
            Assigned, Unassigned -> {
                holder.tv_event.text = OcticonTextView.ICON_PERSON
                holder.tv_event.setTextColor(context.resources.getColor(R.color.text_description))
            }
            Labeled, Unlabeled -> {
                holder.tv_event.text = OcticonTextView.ICON_TAG
                holder.tv_event.setTextColor(context.resources.getColor(R.color.text_description))
            }
            Referenced -> {
                holder.tv_event.text = OcticonTextView.ICON_BOOKMARK
                holder.tv_event.setTextColor(context.resources.getColor(R.color.text_description))
            }
            Milestoned, Demilestoned -> {
                holder.tv_event.text = OcticonTextView.ICON_MILESTONE
                holder.tv_event.setTextColor(context.resources.getColor(R.color.text_description))
            }
            Closed -> {
                holder.tv_event.text = OcticonTextView.ICON_ISSUE_CLOSE
                holder.tv_event.setTextColor(context.resources.getColor(R.color.issue_event_closed))
            }
            Reopened -> {
                holder.tv_event.text = OcticonTextView.ICON_ISSUE_REOPEN
                holder.tv_event.setTextColor(
                        context.resources.getColor(R.color.issue_event_reopened)
                )
            }
            Renamed -> {
                holder.tv_event.text = OcticonTextView.ICON_EDIT
                holder.tv_event.setTextColor(context.resources.getColor(R.color.text_description))
            }
            Merged -> {
                message += String.format(
                        " commit <b>%s</b> into <tt>%s</tt> from <tt>%s</tt>",
                        issueEvent.commitId()!!.substring(0, 6),
                        issue.pullRequest()!!.base()!!.ref(),
                        issue.pullRequest()!!.head()!!.ref()
                )
                holder.tv_event.text = OcticonTextView.ICON_MERGE
                holder.tv_event.setTextColor(context.resources.getColor(R.color.issue_event_merged))
            }
            Locked -> {
                holder.tv_event.text = OcticonTextView.ICON_LOCK
                holder.tv_event.setTextColor(
                        context.resources.getColor(R.color.issue_event_lock))
            }
            Unlocked -> {
                holder.tv_event.text = OcticonTextView.ICON_KEY
                holder.tv_event.setTextColor(
                        context.resources.getColor(R.color.issue_event_lock))
            }
        }

        message += " " + TimeUtils.getRelativeTime(issueEvent.createdAt())
        holder.tv_event.text = Html.fromHtml(message)
    }
}
