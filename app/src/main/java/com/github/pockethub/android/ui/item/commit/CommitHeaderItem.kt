package com.github.pockethub.android.ui.item.commit

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Commit
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.commit_header.view.*

class CommitHeaderItem(private val avatarLoader: AvatarLoader, private val context: Context, val commit: Commit) : Item<CommitHeaderItem.ItemViewHolder>(commit.sha()!!.hashCode().toLong()) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        holder.root.tv_commit_message.text = commit.commit()!!.message()

        val commitAuthor = CommitUtils.getAuthor(commit)
        val commitCommitter = CommitUtils.getCommitter(commit)

        if (commitAuthor != null) {
            CommitUtils.bindAuthor(commit, avatarLoader, holder.root.iv_author)
            holder.root.tv_author.text = commitAuthor
            val styledAuthor = StyledText()
                    .append(context.getString(R.string.authored))

            val commitAuthorDate = CommitUtils.getAuthorDate(commit)
            if (commitAuthorDate != null) {
                styledAuthor.append(' ').append(commitAuthorDate)
            }

            holder.root.tv_author_date.text = styledAuthor
            holder.root.ll_author.visibility = View.VISIBLE
        } else {
            holder.root.ll_author.visibility = View.GONE
        }

        if (isDifferentCommitter(commitAuthor, commitCommitter)) {
            CommitUtils.bindCommitter(commit, avatarLoader, holder.root.iv_committer)
            holder.root.tv_committer.text = commitCommitter
            val styledCommitter = StyledText()
                    .append(context.getString(R.string.committed))

            val commitCommitterDate = CommitUtils.getCommitterDate(commit)
            if (commitCommitterDate != null) {
                styledCommitter.append(' ').append(commitCommitterDate)
            }

            holder.root.tv_commit_date.text = styledCommitter
            holder.root.ll_committer.visibility = View.VISIBLE
        } else {
            holder.root.ll_committer.visibility = View.GONE
        }
    }

    private fun isDifferentCommitter(author: String?, committer: String?) = committer != null && committer != author

    override fun getLayout() = R.layout.commit_header

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView) {

        init {
            root.tv_commit_message.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
