package com.github.pockethub.android.ui.item

import android.text.method.LinkMovementMethod
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.comment.DeleteCommentListener
import com.github.pockethub.android.ui.comment.EditCommentListener
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.GitHubComment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.comment.view.*

class GitHubCommentItem(private val avatarLoader: AvatarLoader, private val imageGetter: HttpImageGetter, private val editCommentListener: EditCommentListener?, private val deleteCommentListener: DeleteCommentListener?, private val username: String, private val canWrite: Boolean, val gitHubComment: GitHubComment) : Item<GitHubCommentItem.ItemViewHolder>(gitHubComment.id()!!) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        imageGetter.bind(holder.root.tv_comment_body, gitHubComment.body(), gitHubComment.id())
        avatarLoader.bind(holder.root.iv_avatar, gitHubComment.user())

        holder.root.tv_comment_author.text = gitHubComment.user()!!.login()
        holder.root.tv_comment_date.text = TimeUtils.getRelativeTime(gitHubComment.updatedAt())

        val canEdit = (canWrite || gitHubComment.user()!!.login() == username) && editCommentListener != null

        val canDelete = (canWrite || gitHubComment.user()!!.login() == username) && deleteCommentListener != null

        if (canDelete) {
            holder.root.iv_delete.visibility = View.VISIBLE
            holder.root.iv_delete.setOnClickListener { _ -> deleteCommentListener!!.onDeleteComment(gitHubComment) }
        } else {
            holder.root.iv_delete.visibility = View.INVISIBLE
        }

        if (canEdit) {
            holder.root.iv_edit.visibility = View.VISIBLE
            holder.root.iv_edit.setOnClickListener { _ -> editCommentListener!!.onEditComment(gitHubComment) }
        } else {
            holder.root.iv_edit.visibility = View.INVISIBLE
        }
    }

    override fun getLayout() = R.layout.comment_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView) {

        init {
            root.tv_comment_body!!.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
