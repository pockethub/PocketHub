package com.github.pockethub.android.ui.item.repository

import android.view.View
import com.github.pockethub.android.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.repo_header_item.view.*

class RepositoryHeaderItem(private val id: String) : Item<RepositoryHeaderItem.ItemViewHolder>(id.hashCode().toLong()) {

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_header.text = id
    }

    override fun getLayout() = R.layout.repo_header_item

    override fun isClickable() = false

    override fun isLongClickable() = false

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
