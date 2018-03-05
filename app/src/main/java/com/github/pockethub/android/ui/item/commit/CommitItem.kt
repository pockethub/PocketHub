package com.github.pockethub.android.ui.item.commit

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Commit
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.commit_item.view.*

class CommitItem(private val avatarLoader: AvatarLoader, val commit: Commit) : Item<CommitItem.ItemViewHolder>(commit.sha()!!.hashCode().toLong()) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        holder.root.tv_commit_id.text = CommitUtils.abbreviate(commit.sha())

        holder.root.tv_commit_author.text = StyledText()
                .bold(CommitUtils.getAuthor(commit))
                .append(' ')
                .append(CommitUtils.getAuthorDate(commit))

        CommitUtils.bindAuthor(commit, avatarLoader, holder.root.iv_avatar)
        holder.root.tv_commit_message!!.text = commit.commit()!!.message()
        holder.root.tv_commit_comments!!.text = CommitUtils.getCommentCount(commit)
    }

    override fun getLayout() = R.layout.commit_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
