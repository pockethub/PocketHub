package com.github.pockethub.android.ui.item.code

import android.content.Context
import com.github.pockethub.android.R
import com.github.pockethub.android.core.code.FullTree
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.util.ServiceUtils
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.folder_item.*

class FolderItem(context: Context, val folder: FullTree.Folder, private val indented: Boolean) : Item(folder.entry.sha()!!.hashCode().toLong()) {

    companion object {

        private const val INDENTED_PADDING = 16
    }

    private val indentedPaddingLeft = ServiceUtils.getIntPixels(context.resources, INDENTED_PADDING)

    override fun getLayout() = R.layout.folder_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_folder.text = CommitUtils.getName(folder.name)
        holder.tv_folders.text = folder.folders.size.toString()
        holder.tv_files.text = folder.files.size.toString()

        val paddingLeft = holder.root.paddingLeft
        val paddingRight = holder.root.paddingRight
        val paddingTop = holder.root.paddingTop
        val paddingBottom = holder.root.paddingBottom

        if (indented) {
            holder.root.setPadding(indentedPaddingLeft, paddingTop, paddingRight, paddingBottom)
        } else {
            holder.root.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        }
    }
}
