package com.github.pockethub.android.ui.item

import android.widget.TextView
import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.User

class ContributorItem(avatarLoader: AvatarLoader, dataItem: User) : UserItem(avatarLoader, dataItem) {

    override fun getLayout() = R.layout.contributor_item

    override fun bind(holder: UserItem.ViewHolder, position: Int) {
        super.bind(holder, position)
        val contributions = holder.root.findViewById<TextView>(R.id.tv_contributions)

        val res = holder.root.resources
        val text = res.getString(R.string.contributions, data.contributions())

        contributions.text = text
    }
}
