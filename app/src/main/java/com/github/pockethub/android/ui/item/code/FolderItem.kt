package com.github.pockethub.android.ui.item.code

import android.content.Context
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.core.code.FullTree
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.util.ServiceUtils
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.folder_item.view.*

class FolderItem(context: Context, val folder: FullTree.Folder, private val indented: Boolean) : Item<FolderItem.ItemViewHolder>(folder.entry.sha()!!.hashCode().toLong()) {

    companion object {

        private const val INDENTED_PADDING = 16
    }

    private val indentedPaddingLeft = ServiceUtils.getIntPixels(context.resources, INDENTED_PADDING)

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_folder.text = CommitUtils.getName(folder.name)
        viewHolder.root.tv_folders.text = folder.folders.size.toString()
        viewHolder.root.tv_files.text = folder.files.size.toString()

        viewHolder.updatePadding(indented, indentedPaddingLeft)
    }

    override fun getLayout() = R.layout.folder_item

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
