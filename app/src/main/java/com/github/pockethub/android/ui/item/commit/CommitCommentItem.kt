package com.github.pockethub.android.ui.item.commit

import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.git.GitComment
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.comment.*

class CommitCommentItem @JvmOverloads constructor(
        private val avatarLoader: AvatarLoader,
        private val imageGetter: HttpImageGetter,
        val comment: GitComment,
        private val isLineComment: Boolean = false
) : Item(comment.id()!!) {

    override fun getLayout() = if (isLineComment) {
        R.layout.diff_comment_item
    } else {
        R.layout.commit_comment_item
    }

    override fun bind(holder: ViewHolder, position: Int) {
        avatarLoader.bind(holder.iv_avatar, comment.user())
        holder.tv_comment_author.text = comment.user()!!.login()
        holder.tv_comment_date.text = TimeUtils.getRelativeTime(comment.updatedAt())
        imageGetter.bind(holder.tv_comment_body, comment.bodyHtml(), comment.id())
    }
}
