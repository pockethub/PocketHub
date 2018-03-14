package com.github.pockethub.android.ui.item.commit

import android.text.method.LinkMovementMethod
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.git.GitComment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.comment.view.*

class CommitCommentItem @JvmOverloads constructor(private val avatarLoader: AvatarLoader, private val imageGetter: HttpImageGetter, val comment: GitComment, private val isLineComment: Boolean = false) : Item<CommitCommentItem.ItemViewHolder>(comment.id()!!) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        avatarLoader.bind(holder.root.iv_avatar, comment.user())
        holder.root.tv_comment_author.text = comment.user()!!.login()
        holder.root.tv_comment_date.text = TimeUtils.getRelativeTime(comment.updatedAt())
        imageGetter.bind(holder.root.tv_comment_body, comment.bodyHtml(), comment.id())
    }

    override fun getLayout() = if (isLineComment) R.layout.diff_comment_item else R.layout.commit_comment_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView) {

        init {
            root.tv_comment_body.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
