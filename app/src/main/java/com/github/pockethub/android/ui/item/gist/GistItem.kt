package com.github.pockethub.android.ui.item.gist

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Gist

class GistItem(avatarLoader: AvatarLoader, dataItem: Gist) : BaseDataItem<Gist, GistItem.ViewHolder>(avatarLoader, dataItem, dataItem.id()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.gist_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val gist = data
        holder.id.text = gist.id()

        val description = gist.description()
        if (!TextUtils.isEmpty(description)) {
            holder.title.text = description
        } else {
            holder.title.setText(R.string.no_description_given)
        }

        val user = gist.owner()
        avatarLoader.bind(holder.avatar, user)

        val authorText = StyledText()
        if (user != null) {
            authorText.bold(user.login())
        } else {
            val res = holder.root.resources
            authorText.bold(res.getString(R.string.anonymous))
        }
        authorText.append(' ')
        authorText.append(gist.createdAt())
        holder.author.text = authorText

        holder.comments.text = gist.comments().toString()
        holder.files.text = gist.files()!!.size.toString()
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_gist_id)
        lateinit var id: TextView

        @BindView(R.id.tv_gist_title)
        lateinit var title: TextView

        @BindView(R.id.tv_gist_author)
        lateinit var author: TextView

        @BindView(R.id.tv_gist_comments)
        lateinit var comments: TextView

        @BindView(R.id.tv_gist_files)
        lateinit var files: TextView

        @BindView(R.id.iv_avatar)
        lateinit var avatar: ImageView
    }
}
