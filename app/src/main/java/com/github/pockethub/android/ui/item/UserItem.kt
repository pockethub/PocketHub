package com.github.pockethub.android.ui.item

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.User
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_item.view.*

open class UserItem(private val avatarLoader: AvatarLoader, open val user: User) : Item<UserItem.ItemViewHolder>(user.id()!!) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        avatarLoader.bind(holder.root.iv_avatar, user)
        holder.root.tv_login.text = user.login()
    }

    override fun getLayout() = R.layout.user_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
