package com.github.pockethub.android.ui.item.commit

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.commit.DiffStyler
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.commit_diff_line.view.*

class CommitFileLineItem(private val diffStyler: DiffStyler, val line: CharSequence) : Item<CommitFileLineItem.ItemViewHolder>(line.hashCode().toLong()) {

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_diff.text = line
        diffStyler.updateColors(line, viewHolder.root.tv_diff)
    }

    override fun getLayout() = R.layout.commit_diff_line

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
