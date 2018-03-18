package com.github.pockethub.android.ui.item.notification

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.notification.NotificationListFragment
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.NotificationThread
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.notification_item.*

class NotificationItem(
        val notificationThread: NotificationThread,
        private val notificationReadListener: NotificationListFragment
) : Item(notificationThread.id()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.notification_item

    override fun bind(holder: ViewHolder, position: Int) {
        val type = notificationThread.subject()!!.type()
        when (type) {
            "Issue" -> holder.tv_notification_icon.text = OcticonTextView.ICON_ISSUE_OPEN
            "Release" -> holder.tv_notification_icon.text = OcticonTextView.ICON_TAG
            else -> holder.tv_notification_icon.text = OcticonTextView.ICON_PULL_REQUEST
        }

        holder.tv_notification_title.text = notificationThread.subject()!!.title()
        holder.tv_notification_date.text = TimeUtils.getRelativeTime(notificationThread.updatedAt())

        holder.tv_notification_read_icon.text = OcticonTextView.ICON_READ
        holder.tv_notification_read_icon.visibility = if (notificationThread.unread()!!) {
            View.VISIBLE
        } else {
            View.GONE
        }

        if (!holder.tv_notification_read_icon.hasOnClickListeners()) {
            holder.tv_notification_read_icon.setOnClickListener { _ ->
                notificationReadListener.readNotification(notificationThread)
            }
        }
    }
}
