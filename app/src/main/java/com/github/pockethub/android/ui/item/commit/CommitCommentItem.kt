package com.github.pockethub.android.ui.item.commit

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.ui.view.LinkTextView
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.git.GitComment

class CommitCommentItem @JvmOverloads constructor(avatars: AvatarLoader, private val imageGetter: HttpImageGetter, comment: GitComment, private val isLineComment: Boolean = false) : BaseDataItem<GitComment, CommitCommentItem.ViewHolder>(avatars, comment, comment.id()!!) {

    override fun getLayout() = if (isLineComment) R.layout.diff_comment_item else R.layout.commit_comment_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val comment = data
        avatarLoader.bind(holder.avatar, comment.user())
        holder.author.text = comment.user()!!.login()
        holder.date.text = TimeUtils.getRelativeTime(comment.updatedAt())
        imageGetter.bind(holder.body, comment.bodyHtml(), comment.id())
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_comment_body)
        lateinit var body: LinkTextView

        @BindView(R.id.iv_avatar)
        lateinit var avatar: ImageView

        @BindView(R.id.tv_comment_author)
        lateinit var author: TextView

        @BindView(R.id.tv_comment_date)
        lateinit var date: TextView
    }
}
