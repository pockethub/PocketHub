package com.github.pockethub.android.ui.item.notification

import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.ui.notification.NotificationListFragment
import com.meisolsson.githubsdk.model.Repository

class NotificationHeaderItem(dataItem: Repository, private val notificationReadListener: NotificationListFragment) : BaseDataItem<Repository, NotificationHeaderItem.ViewHolder>(null, dataItem, dataItem.id()!!) {

    override fun getLayout() = R.layout.notification_item_header

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val repository = data
        holder.name.text = repository.fullName()

        holder.readIcon.text = "\uf03a"
        if (!holder.readIcon.hasOnClickListeners()) {
            holder.readIcon.setOnClickListener { _ -> notificationReadListener.readNotifications(repository) }
        }
    }

    override fun isClickable() = false

    override fun isLongClickable() = false

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_name)
        lateinit var name: TextView

        @BindView(R.id.tv_notifications_read_icon)
        lateinit var readIcon: TextView
    }
}
