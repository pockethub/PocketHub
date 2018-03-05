package com.github.pockethub.android.ui.item.gist

import android.view.View
import com.github.pockethub.android.R
import com.meisolsson.githubsdk.model.GistFile
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.gist_file_item.view.*

class GistFileItem(val gistFile: GistFile) : Item<GistFileItem.ItemViewHolder>(gistFile.filename()!!.hashCode().toLong()) {

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_file.text = gistFile.filename()
    }

    override fun getLayout() = R.layout.gist_file_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
