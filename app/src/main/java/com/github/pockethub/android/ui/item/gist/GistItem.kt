package com.github.pockethub.android.ui.item.gist

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Gist
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.gist_item.view.*

class GistItem(private val avatarLoader: AvatarLoader, val gist: Gist) : Item<GistItem.ItemViewHolder>(gist.id()!!.hashCode().toLong()) {

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_gist_id.text = gist.id()

        val description = gist.description()
        if (!description.isNullOrEmpty()) {
            viewHolder.root.tv_gist_title.text = description
        } else {
            viewHolder.root.tv_gist_title.setText(R.string.no_description_given)
        }

        val user = gist.owner()
        avatarLoader.bind(viewHolder.root.iv_avatar, user)

        val authorText = StyledText()
        if (user != null) {
            authorText.bold(user.login())
        } else {
            val res = viewHolder.root.resources
            authorText.bold(res.getString(R.string.anonymous))
        }
        authorText.append(' ')
                .append(gist.createdAt())
        viewHolder.root.tv_gist_author.text = authorText

        viewHolder.root.tv_gist_comments.text = gist.comments().toString()
        viewHolder.root.tv_gist_files.text = gist.files()!!.size.toString()
    }

    override fun getLayout() = R.layout.gist_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
