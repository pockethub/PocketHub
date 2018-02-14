package com.github.pockethub.android.ui.item.issue

import android.content.Context
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.IssueEvent
import com.meisolsson.githubsdk.model.IssueEventType.*
import kotlinx.android.synthetic.main.comment_event.view.*

class IssueEventItem(avatarLoader: AvatarLoader, private val context: Context, private val issue: Issue, dataItem: IssueEvent)
    : BaseDataItem<IssueEvent, IssueEventItem.ViewHolder>(avatarLoader, dataItem, dataItem.id()!!.toLong()) {

    override fun bind(holder: ViewHolder, position: Int) {
        val event = data
        var message = String.format("<b>%s</b> %s", event.actor()!!.login(), event.event())
        avatarLoader.bind(holder.itemView.iv_avatar, event.actor())

        val eventType = event.event()

        when (eventType) {
            Assigned, Unassigned -> {
                holder.event.text = OcticonTextView.ICON_PERSON
                holder.event.setTextColor(
                        context.resources.getColor(R.color.text_description))
            }
            Labeled, Unlabeled -> {
                holder.event.text = OcticonTextView.ICON_TAG
                holder.event.setTextColor(
                        context.resources.getColor(R.color.text_description))
            }
            Referenced -> {
                holder.event.text = OcticonTextView.ICON_BOOKMARK
                holder.event.setTextColor(
                        context.resources.getColor(R.color.text_description))
            }
            Milestoned, Demilestoned -> {
                holder.event.text = OcticonTextView.ICON_MILESTONE
                holder.event.setTextColor(
                        context.resources.getColor(R.color.text_description))
            }
            Closed -> {
                holder.event.text = OcticonTextView.ICON_ISSUE_CLOSE
                holder.event.setTextColor(
                        context.resources.getColor(R.color.issue_event_closed))
            }
            Reopened -> {
                holder.event.text = OcticonTextView.ICON_ISSUE_REOPEN
                holder.event.setTextColor(
                        context.resources.getColor(R.color.issue_event_reopened))
            }
            Renamed -> {
                holder.event.text = OcticonTextView.ICON_EDIT
                holder.event.setTextColor(
                        context.resources.getColor(R.color.text_description))
            }
            Merged -> {
                message += String.format(" commit <b>%s</b> into <tt>%s</tt> from <tt>%s</tt>", event.commitId()!!.substring(0, 6),
                        issue.pullRequest()!!.base()!!.ref(),
                        issue.pullRequest()!!.head()!!.ref())
                holder.event.text = OcticonTextView.ICON_MERGE
                holder.event.setTextColor(
                        context.resources.getColor(R.color.issue_event_merged))
            }
            Locked -> {
                holder.event.text = OcticonTextView.ICON_LOCK
                holder.event.setTextColor(
                        context.resources.getColor(R.color.issue_event_lock))
            }
            Unlocked -> {
                holder.event.text = OcticonTextView.ICON_KEY
                holder.event.setTextColor(
                        context.resources.getColor(R.color.issue_event_lock))
            }
        }

        message += " " + TimeUtils.getRelativeTime(event.createdAt())
        holder.event.text = Html.fromHtml(message)
    }

    override fun getLayout(): Int = R.layout.comment_event_item

    override fun createViewHolder(itemView: View): ViewHolder = ViewHolder(itemView)

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        var icon: TextView
        var event: TextView
        var avatar: ImageView

        init {
            icon = rootView.tv_event_icon
            event = rootView.tv_event
            avatar = rootView.iv_avatar
        }
    }
}
