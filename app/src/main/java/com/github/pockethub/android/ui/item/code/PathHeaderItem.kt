package com.github.pockethub.android.ui.item.code

import android.text.method.LinkMovementMethod
import android.view.View
import com.github.pockethub.android.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.path_item.view.*

class PathHeaderItem(private val path: CharSequence) : Item<PathHeaderItem.ItemViewHolder>(path.hashCode().toLong()) {

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_path.text = path
    }

    override fun getLayout() = R.layout.path_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView) {

        init {
            root.tv_path.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
