package com.github.pockethub.android.ui.item.gist

import android.text.TextUtils
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Gist
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.gist_item.*

class GistItem(
        private val avatarLoader: AvatarLoader,
        val gist: Gist
) : Item(gist.id()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.gist_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_gist_id.text = gist.id()

        val description = gist.description()
        if (!TextUtils.isEmpty(description)) {
            holder.tv_gist_title.text = description
        } else {
            holder.tv_gist_title.setText(R.string.no_description_given)
        }

        val user = gist.owner()
        avatarLoader.bind(holder.iv_avatar, user)

        val authorText = StyledText()
        if (user != null) {
            authorText.bold(user.login())
        } else {
            val res = holder.root.resources
            authorText.bold(res.getString(R.string.anonymous))
        }
        authorText.append(' ')
        authorText.append(gist.createdAt())
        holder.tv_gist_author.text = authorText

        holder.tv_gist_comments.text = gist.comments().toString()
        holder.tv_gist_files.text = gist.files()!!.size.toString()
    }
}
