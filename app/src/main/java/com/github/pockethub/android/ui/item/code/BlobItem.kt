package com.github.pockethub.android.ui.item.code

import android.content.Context
import android.text.format.Formatter
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.core.code.FullTree
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.util.ServiceUtils

class BlobItem(private val context: Context, dataItem: FullTree.Entry, private val indented: Boolean) : BaseDataItem<FullTree.Entry, BlobItem.ViewHolder>(null, dataItem, dataItem.entry.sha()!!.hashCode().toLong()) {

    companion object {

        private const val INDENTED_PADDING = 16
    }

    private val indentedPaddingLeft = ServiceUtils.getIntPixels(context.resources, INDENTED_PADDING)

    override fun getLayout() = R.layout.blob_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val file = data
        holder.file.text = file.name
        holder.size.text = Formatter.formatFileSize(context, file.entry.size()!!.toLong())

        holder.updatePadding(indented, indentedPaddingLeft)
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        private val paddingLeft = rootView.paddingLeft
        private val paddingRight = rootView.paddingRight
        private val paddingTop = rootView.paddingTop
        private val paddingBottom = rootView.paddingBottom

        @BindView(R.id.tv_file)
        lateinit var file: TextView

        @BindView(R.id.tv_size)
        lateinit var size: TextView

        internal fun updatePadding(indented: Boolean, indentedPaddingLeft: Int) {
            if (indented) {
                root.setPadding(indentedPaddingLeft, paddingTop, paddingRight, paddingBottom)
            } else {
                root.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
            }
        }
    }
}
