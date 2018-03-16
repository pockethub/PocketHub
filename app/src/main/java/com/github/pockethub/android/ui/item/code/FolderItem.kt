package com.github.pockethub.android.ui.item.code

import android.content.Context
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.core.code.FullTree
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.util.ServiceUtils

class FolderItem(context: Context, dataItem: FullTree.Folder, private val indented: Boolean) : BaseDataItem<FullTree.Folder, FolderItem.ViewHolder>(null, dataItem, dataItem.entry.sha()!!.hashCode().toLong()) {

    companion object {

        private const val INDENTED_PADDING = 16
    }

    private val indentedPaddingLeft = ServiceUtils.getIntPixels(context.resources, INDENTED_PADDING)

    override fun getLayout() = R.layout.folder_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val folder = data
        holder.folder.text = CommitUtils.getName(folder.name)
        holder.folders.text = folder.folders.size.toString()
        holder.files.text = folder.files.size.toString()

        holder.updatePadding(indented, indentedPaddingLeft)
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        private val paddingLeft = rootView.paddingLeft
        private val paddingRight = rootView.paddingRight
        private val paddingTop = rootView.paddingTop
        private val paddingBottom = rootView.paddingBottom

        @BindView(R.id.tv_folder)
        lateinit var folder: TextView

        @BindView(R.id.tv_folders)
        lateinit var folders: TextView

        @BindView(R.id.tv_files)
        lateinit var files: TextView

        internal fun updatePadding(indented: Boolean, indentedPaddingLeft: Int) {
            if (indented) {
                root.setPadding(indentedPaddingLeft, paddingTop, paddingRight, paddingBottom)
            } else {
                root.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
            }
        }
    }
}
