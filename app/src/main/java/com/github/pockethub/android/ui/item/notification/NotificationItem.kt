package com.github.pockethub.android.ui.item.notification

import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.ui.notification.NotificationListFragment
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.NotificationThread

class NotificationItem(dataItem: NotificationThread, private val notificationReadListener: NotificationListFragment) : BaseDataItem<NotificationThread, NotificationItem.ViewHolder>(null, dataItem, dataItem.id()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.notification_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val thread = data

        val type = thread.subject()!!.type()
        when (type) {
            "Issue" -> holder.icon.text = OcticonTextView.ICON_ISSUE_OPEN
            "Release" -> holder.icon.text = OcticonTextView.ICON_TAG
            else -> holder.icon.text = OcticonTextView.ICON_PULL_REQUEST
        }

        holder.title.text = thread.subject()!!.title()
        holder.date.text = TimeUtils.getRelativeTime(thread.updatedAt())

        holder.readIcon.text = OcticonTextView.ICON_READ
        holder.readIcon.visibility = if (thread.unread()!!) View.VISIBLE else View.GONE

        if (!holder.readIcon.hasOnClickListeners()) {
            holder.readIcon.setOnClickListener { _ -> notificationReadListener.readNotification(data) }
        }
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_notification_icon)
        lateinit var icon: TextView

        @BindView(R.id.tv_notification_title)
        lateinit var title: TextView

        @BindView(R.id.tv_notification_date)
        lateinit var date: TextView

        @BindView(R.id.tv_notification_read_icon)
        lateinit var readIcon: TextView
    }
}
