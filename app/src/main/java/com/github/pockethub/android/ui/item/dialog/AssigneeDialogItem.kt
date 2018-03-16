package com.github.pockethub.android.ui.item.dialog

import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.User

class AssigneeDialogItem(avatarLoader: AvatarLoader, dataItem: User, private val selected: Int) : BaseDataItem<User, AssigneeDialogItem.ViewHolder>(avatarLoader, dataItem, dataItem.id()!!) {

    override fun getLayout() = R.layout.collaborator_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        holder.login.text = data.login()
        holder.selected.isChecked = selected == position
        avatarLoader.bind(holder.avatar, data)
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_login)
        lateinit var login: TextView

        @BindView(R.id.iv_avatar)
        lateinit var avatar: ImageView

        @BindView(R.id.rb_selected)
        lateinit var selected: RadioButton
    }
}
