package com.github.pockethub.android.ui.item

import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.User
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.user_item.*

open class UserItem(private val avatarLoader: AvatarLoader, val user: User) : Item(user.id()!!) {

    override fun getLayout() = R.layout.user_item

    override fun bind(holder: ViewHolder, position: Int) {
        avatarLoader.bind(holder.iv_avatar, user)
        holder.tv_login.text = user.login()
    }
}
