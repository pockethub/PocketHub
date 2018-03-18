package com.github.pockethub.android.ui.item.code

import android.content.Context
import android.text.format.Formatter
import com.github.pockethub.android.R
import com.github.pockethub.android.core.code.FullTree
import com.github.pockethub.android.util.ServiceUtils
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.blob_item.*

class BlobItem(
        private val context: Context,
        val file: FullTree.Entry,
        private val indented: Boolean
) : Item(file.entry.sha()!!.hashCode().toLong()) {

    companion object {

        private const val INDENTED_PADDING = 16
    }

    private val indentedPaddingLeft = ServiceUtils.getIntPixels(context.resources, INDENTED_PADDING)

    override fun getLayout() = R.layout.blob_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_file.text = file.name
        holder.tv_size.text = Formatter.formatFileSize(context, file.entry.size()!!.toLong())

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
