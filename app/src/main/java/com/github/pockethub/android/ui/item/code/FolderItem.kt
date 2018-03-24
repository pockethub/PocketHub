package com.github.pockethub.android.ui.item.code

import com.github.pockethub.android.R
import com.github.pockethub.android.core.code.FullTree
import com.github.pockethub.android.core.commit.CommitUtils
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.folder_item.*

class FolderItem(val folder: FullTree.Folder) : Item(folder.entry.sha()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.folder_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_folder.text = CommitUtils.getName(folder.name)
        holder.tv_folders.text = folder.folders.size.toString()
        holder.tv_files.text = folder.files.size.toString()
    }
}
