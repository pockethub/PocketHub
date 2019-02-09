package com.github.pockethub.android.ui.item.commit

import android.content.Context
import android.view.View
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.R
import com.github.pockethub.android.util.android.text.append
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Commit
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.commit_header.*

class CommitHeaderItem(
        private val avatarLoader: AvatarLoader,
        private val context: Context,
        val commit: Commit
) : Item(commit.sha()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.commit_header

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_commit_message.text = commit.commit()!!.message()

        val commitAuthor = CommitUtils.getAuthor(commit)
        val commitCommitter = CommitUtils.getCommitter(commit)

        if (commitAuthor != null) {
            CommitUtils.bindAuthor(commit, avatarLoader, holder.iv_author)
            holder.tv_author.text = commitAuthor
            holder.tv_author_date.text = buildSpannedString {
                append(context.getString(R.string.authored))

                val commitAuthorDate = CommitUtils.getAuthorDate(commit)
                if (commitAuthorDate != null) {
                    append(' ')
                    append(commitAuthorDate)
                }
            }
            holder.ll_author.visibility = View.VISIBLE
        } else {
            holder.ll_author.visibility = View.GONE
        }

        if (isDifferentCommitter(commitAuthor, commitCommitter)) {
            CommitUtils.bindCommitter(commit, avatarLoader, holder.iv_committer)
            holder.tv_committer.text = commitCommitter
            holder.tv_commit_date.text = buildSpannedString {
                append(context.getString(R.string.committed))

                val commitCommitterDate = CommitUtils.getCommitterDate(commit)
                if (commitCommitterDate != null) {
                    append(' ')
                    append(commitCommitterDate)
                }
            }
            holder.ll_committer.visibility = View.VISIBLE
        } else {
            holder.ll_committer.visibility = View.GONE
        }
    }

    private fun isDifferentCommitter(author: String?, committer: String?): Boolean {
        return committer != null && committer != author
    }
}
