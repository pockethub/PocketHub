package com.github.pockethub.android.ui.item.dialog

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.User
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.collaborator_item.view.*

class AssigneeDialogItem(private val avatarLoader: AvatarLoader, val user: User, private val selected: Int) : Item<AssigneeDialogItem.ItemViewHolder>(user.id()!!) {

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_login.text = user.login()
        viewHolder.root.rb_selected.isChecked = selected == position
        avatarLoader.bind(viewHolder.root.iv_avatar, user)
    }

    override fun getLayout() = R.layout.collaborator_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
