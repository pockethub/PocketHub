package com.github.pockethub.android.ui.item.commit

import android.content.Context
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.ui.StyledText
import com.meisolsson.githubsdk.model.Commit
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.commit_parent_item.view.*

class CommitParentItem(private val context: Context, val commit: Commit) : Item<CommitParentItem.ItemViewHolder>(commit.sha()!!.hashCode().toLong()) {

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_commit_id.text = StyledText()
                .append(context.getString(R.string.parent_prefix))
                .monospace(CommitUtils.abbreviate(commit.sha()))
                .underlineAll()
    }

    override fun getLayout() = R.layout.commit_parent_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
