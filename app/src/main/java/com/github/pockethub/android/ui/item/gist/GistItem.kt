package com.github.pockethub.android.ui.item.gist

import android.text.TextUtils
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.R
import com.github.pockethub.android.util.android.text.append
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

        holder.tv_gist_author.text = buildSpannedString {
            if (user != null) {
                bold {
                    append(user.login())
                }
            } else {
                val res = holder.root.resources
                bold {
                    append(res.getString(R.string.anonymous))
                }
            }
            append(' ')
            append(gist.createdAt()!!)
        }

        holder.tv_gist_comments.text = gist.comments().toString()
        holder.tv_gist_files.text = gist.files()!!.size.toString()
    }
}
