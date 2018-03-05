package com.github.pockethub.android.ui.item.code

import android.content.Context
import android.text.format.Formatter
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.core.code.FullTree
import com.github.pockethub.android.util.ServiceUtils
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.blob_item.view.*

class BlobItem(private val context: Context, val file: FullTree.Entry, private val indented: Boolean) : Item<BlobItem.ItemViewHolder>(file.entry.sha()!!.hashCode().toLong()) {

    companion object {

        private const val INDENTED_PADDING = 16
    }

    private val indentedPaddingLeft = ServiceUtils.getIntPixels(context.resources, INDENTED_PADDING)

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_file.text = file.name
        viewHolder.root.tv_size.text = Formatter.formatFileSize(context, file.entry.size()!!.toLong())

        viewHolder.updatePadding(indented, indentedPaddingLeft)
    }

    override fun getLayout() = R.layout.blob_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView) {

        internal fun updatePadding(indented: Boolean, indentedPaddingLeft: Int) {
            if (indented) {
                root.setPadding(indentedPaddingLeft, root.paddingTop, root.paddingRight, root.paddingBottom)
            } else {
                root.setPadding(root.paddingLeft, root.paddingTop, root.paddingRight, root.paddingBottom)
            }
        }
    }
}
