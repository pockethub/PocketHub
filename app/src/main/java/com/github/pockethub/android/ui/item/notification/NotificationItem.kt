package com.github.pockethub.android.ui.item.notification

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.notification.NotificationListFragment
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.NotificationThread
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.notification_item.view.*

class NotificationItem(val thread: NotificationThread, private val notificationReadListener: NotificationListFragment) : Item<NotificationItem.ItemViewHolder>(thread.id()!!.hashCode().toLong()) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        val type = thread.subject()!!.type()
        when (type) {
            "Issue" -> holder.root.tv_notification_icon.text = OcticonTextView.ICON_ISSUE_OPEN
            "Release" -> holder.root.tv_notification_icon.text = OcticonTextView.ICON_TAG
            else -> holder.root.tv_notification_icon.text = OcticonTextView.ICON_PULL_REQUEST
        }

        holder.root.tv_notification_title.text = thread.subject()!!.title()
        holder.root.tv_notification_date.text = TimeUtils.getRelativeTime(thread.updatedAt())

        holder.root.tv_notification_read_icon.text = OcticonTextView.ICON_READ
        holder.root.tv_notification_read_icon.visibility = if (thread.unread()!!) View.VISIBLE else View.GONE

        if (!holder.root.tv_notification_read_icon.hasOnClickListeners()) {
            holder.root.tv_notification_read_icon.setOnClickListener { _ -> notificationReadListener.readNotification(thread) }
        }
    }

    override fun getLayout() = R.layout.notification_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
