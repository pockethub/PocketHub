package com.github.pockethub.android.ui.item

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.comment.DeleteCommentListener
import com.github.pockethub.android.ui.comment.EditCommentListener
import com.github.pockethub.android.ui.view.LinkTextView
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.GitHubComment

class GitHubCommentItem(avatarLoader: AvatarLoader, private val imageGetter: HttpImageGetter, private val editCommentListener: EditCommentListener?, private val deleteCommentListener: DeleteCommentListener?, private val username: String, private val canWrite: Boolean, dataItem: GitHubComment) : BaseDataItem<GitHubComment, GitHubCommentItem.ViewHolder>(avatarLoader, dataItem, dataItem.id()!!) {

    override fun getLayout() = R.layout.comment_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        imageGetter.bind(holder.body, data.body(), data.id())
        avatarLoader.bind(holder.avatar, data.user())

        holder.author.text = data.user()!!.login()
        holder.date.text = TimeUtils.getRelativeTime(data.updatedAt())

        val canEdit = (canWrite || data.user()!!.login() == username) && editCommentListener != null

        val canDelete = (canWrite || data.user()!!.login() == username) && deleteCommentListener != null

        if (canDelete) {
            holder.deleteIcon.visibility = View.VISIBLE
            holder.deleteIcon.setOnClickListener { _ -> deleteCommentListener!!.onDeleteComment(data) }
        } else {
            holder.deleteIcon.visibility = View.INVISIBLE
        }

        if (canEdit) {
            holder.editIcon.visibility = View.VISIBLE
            holder.editIcon.setOnClickListener { _ -> editCommentListener!!.onEditComment(data) }
        } else {
            holder.editIcon.visibility = View.INVISIBLE
        }
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_comment_body)
        lateinit var body: LinkTextView

        @BindView(R.id.tv_comment_author)
        lateinit var author: TextView

        @BindView(R.id.tv_comment_date)
        lateinit var date: TextView

        @BindView(R.id.iv_avatar)
        lateinit var avatar: ImageView

        @BindView(R.id.iv_edit)
        lateinit var editIcon: ImageView

        @BindView(R.id.iv_delete)
        lateinit var deleteIcon: ImageView
    }
}
