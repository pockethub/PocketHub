package com.github.pockethub.android.ui.item.commit

import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.R
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Commit
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.commit_item.*

class CommitItem(
        private val avatarLoader: AvatarLoader,
        val commit: Commit
) : Item(commit.sha()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.commit_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_commit_id.text = CommitUtils.abbreviate(commit.sha())

        holder.tv_commit_author.text = buildSpannedString {
            bold {
                append(CommitUtils.getAuthor(commit))
            }
            append(" ${CommitUtils.getAuthorDate(commit)}")
        }

        CommitUtils.bindAuthor(commit, avatarLoader, holder.iv_avatar)
        holder.tv_commit_message.text = commit.commit()!!.message()
        holder.tv_commit_comments.text = CommitUtils.getCommentCount(commit)
    }
}
