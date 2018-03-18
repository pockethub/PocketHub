package com.github.pockethub.android.ui.item

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.comment.DeleteCommentListener
import com.github.pockethub.android.ui.comment.EditCommentListener
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.GitHubComment
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.comment.*

class GitHubCommentItem(
        private val avatarLoader: AvatarLoader,
        private val imageGetter: HttpImageGetter,
        private val editCommentListener: EditCommentListener?,
        private val deleteCommentListener: DeleteCommentListener?,
        private val username: String,
        private val canWrite: Boolean,
        val gitHubComment: GitHubComment
) : Item(gitHubComment.id()!!) {

    override fun getLayout() = R.layout.comment_item

    override fun bind(holder: ViewHolder, position: Int) {
        imageGetter.bind(holder.tv_comment_body, gitHubComment.body(), gitHubComment.id())
        avatarLoader.bind(holder.iv_avatar, gitHubComment.user())

        holder.tv_comment_author.text = gitHubComment.user()!!.login()
        holder.tv_comment_date.text = TimeUtils.getRelativeTime(gitHubComment.updatedAt())

        val canEdit = (canWrite || gitHubComment.user()!!.login() == username) &&
                editCommentListener != null

        val canDelete = (canWrite || gitHubComment.user()!!.login() == username) &&
                deleteCommentListener != null

        if (canDelete) {
            holder.iv_delete.visibility = View.VISIBLE
            holder.iv_delete.setOnClickListener { _ ->
                deleteCommentListener!!.onDeleteComment(gitHubComment)
            }
        } else {
            holder.iv_delete.visibility = View.INVISIBLE
        }

        if (canEdit) {
            holder.iv_edit.visibility = View.VISIBLE
            holder.iv_edit.setOnClickListener { _ ->
                editCommentListener!!.onEditComment(gitHubComment)
            }
        } else {
            holder.iv_edit.visibility = View.INVISIBLE
        }
    }
}
