package com.github.pockethub.android.ui.item.commit

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Commit

class CommitItem(avatars: AvatarLoader, dataItem: Commit) : BaseDataItem<Commit, CommitItem.ViewHolder>(avatars, dataItem, dataItem.sha()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.commit_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        holder.id.text = CommitUtils.abbreviate(data.sha())

        val authorText = StyledText()
        authorText.bold(CommitUtils.getAuthor(data))
        authorText.append(' ')
        authorText.append(CommitUtils.getAuthorDate(data))
        holder.author.text = authorText

        CommitUtils.bindAuthor(data, avatarLoader, holder.avatar)
        holder.message.text = data.commit()!!.message()
        holder.comments.text = CommitUtils.getCommentCount(data)
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_commit_id)
        lateinit var id: TextView

        @BindView(R.id.tv_commit_author)
        lateinit var author: TextView

        @BindView(R.id.iv_avatar)
        lateinit var avatar: ImageView

        @BindView(R.id.tv_commit_message)
        lateinit var message: TextView

        @BindView(R.id.tv_commit_comments)
        lateinit var comments: TextView
    }
}
