package com.github.pockethub.android.ui.item.notification

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.notification.NotificationListFragment
import com.meisolsson.githubsdk.model.Repository
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.notification_item_header.view.*

class NotificationHeaderItem(val repository: Repository, private val notificationReadListener: NotificationListFragment) : Item<NotificationHeaderItem.ItemViewHolder>(repository.id()!!) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        holder.root.tv_name.text = repository.fullName()

        holder.root.tv_notifications_read_icon.text = "\uf03a"
        if (!holder.root.tv_notifications_read_icon.hasOnClickListeners()) {
            holder.root.tv_notifications_read_icon.setOnClickListener { _ -> notificationReadListener.readNotifications(repository) }
        }
    }

    override fun getLayout() = R.layout.notification_item_header

    override fun isClickable() = false

    override fun isLongClickable() = false

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
