package com.github.pockethub.android.ui.item.dialog

import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.User
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.collaborator_item.*

class AssigneeDialogItem(
        private val avatarLoader: AvatarLoader,
        val user: User,
        private val selected: Int
) : Item(user.id()!!) {

    override fun getLayout() = R.layout.collaborator_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_login.text = user.login()
        holder.rb_selected.isChecked = selected == position
        avatarLoader.bind(holder.iv_avatar, user)
    }
}
