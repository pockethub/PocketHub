package com.github.pockethub.android.ui.item.notification

import com.github.pockethub.android.R
import com.github.pockethub.android.ui.notification.NotificationListFragment
import com.meisolsson.githubsdk.model.Repository
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.notification_item_header.*

class NotificationHeaderItem(
        val repository: Repository,
        private val notificationReadListener: NotificationListFragment
) : Item(repository.id()!!) {

    override fun getLayout() = R.layout.notification_item_header

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_name.text = repository.fullName()

        holder.tv_notifications_read_icon.text = "\uf03a"
        if (!holder.tv_notifications_read_icon.hasOnClickListeners()) {
            holder.tv_notifications_read_icon.setOnClickListener { _ ->
                notificationReadListener.readNotifications(repository)
            }
        }
    }

    override fun isClickable() = false

    override fun isLongClickable() = false
}
