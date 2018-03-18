package com.github.pockethub.android.ui.item

import android.widget.TextView
import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.User
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class ContributorItem(avatarLoader: AvatarLoader, user: User) : UserItem(avatarLoader, user) {

    override fun getLayout() = R.layout.contributor_item

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        val contributions = holder.root.findViewById<TextView>(R.id.tv_contributions)

        val res = holder.root.resources
        val text = res.getString(R.string.contributions, user.contributions())

        contributions.text = text
    }
}
