package com.github.pockethub.android.ui.item

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.User

open class UserItem(avatarLoader: AvatarLoader, dataItem: User) : BaseDataItem<User, UserItem.ViewHolder>(avatarLoader, dataItem, dataItem.id()!!) {

    override fun getLayout() = R.layout.user_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        avatarLoader.bind(holder.avatar, data)
        holder.login.text = data.login()
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.iv_avatar)
        lateinit var avatar: ImageView

        @BindView(R.id.tv_login)
        lateinit var login: TextView
    }
}
