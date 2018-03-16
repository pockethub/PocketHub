package com.github.pockethub.android.ui.item.commit

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.ui.view.LinkTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Commit

class CommitHeaderItem(avatarLoader: AvatarLoader, private val context: Context, dataItem: Commit) : BaseDataItem<Commit, CommitHeaderItem.ViewHolder>(avatarLoader, dataItem, dataItem.sha()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.commit_header

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val commit = data
        holder.commitMessage.text = commit.commit()!!.message()

        val commitAuthor = CommitUtils.getAuthor(commit)
        val commitCommitter = CommitUtils.getCommitter(commit)

        if (commitAuthor != null) {
            CommitUtils.bindAuthor(commit, avatarLoader, holder.authorAvatar)
            holder.authorName.text = commitAuthor
            val styledAuthor = StyledText()
            styledAuthor.append(context.getString(R.string.authored))

            val commitAuthorDate = CommitUtils.getAuthorDate(commit)
            if (commitAuthorDate != null) {
                styledAuthor.append(' ').append(commitAuthorDate)
            }

            holder.authorDate.text = styledAuthor
            holder.authorArea.visibility = View.VISIBLE
        } else {
            holder.authorArea.visibility = View.GONE
        }

        if (isDifferentCommitter(commitAuthor, commitCommitter)) {
            CommitUtils.bindCommitter(commit, avatarLoader, holder.committerAvatar)
            holder.committerName.text = commitCommitter
            val styledCommitter = StyledText()
            styledCommitter.append(context.getString(R.string.committed))

            val commitCommitterDate = CommitUtils.getCommitterDate(commit)
            if (commitCommitterDate != null) {
                styledCommitter.append(' ').append(commitCommitterDate)
            }

            holder.committerDate.text = styledCommitter
            holder.committerArea.visibility = View.VISIBLE
        } else {
            holder.committerArea.visibility = View.GONE
        }
    }

    private fun isDifferentCommitter(author: String?, committer: String?): Boolean {
        return committer != null && committer != author
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.ll_author)
        lateinit var authorArea: LinearLayout

        @BindView(R.id.iv_author)
        lateinit var authorAvatar: ImageView

        @BindView(R.id.tv_author)
        lateinit var authorName: TextView

        @BindView(R.id.tv_author_date)
        lateinit var authorDate: TextView

        @BindView(R.id.ll_committer)
        lateinit var committerArea: LinearLayout

        @BindView(R.id.iv_committer)
        lateinit var committerAvatar: ImageView

        @BindView(R.id.tv_committer)
        lateinit var committerName: TextView

        @BindView(R.id.tv_commit_date)
        lateinit var committerDate: TextView

        @BindView(R.id.tv_commit_message)
        lateinit var commitMessage: LinkTextView
    }
}
