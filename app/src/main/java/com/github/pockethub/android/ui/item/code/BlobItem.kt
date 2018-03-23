package com.github.pockethub.android.ui.item.code

import android.content.Context
import android.text.format.Formatter
import com.github.pockethub.android.R
import com.github.pockethub.android.core.code.FullTree
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.blob_item.*

class BlobItem(
        private val context: Context,
        val file: FullTree.Entry
) : Item(file.entry.sha()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.blob_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_file.text = file.name
        holder.tv_size.text = Formatter.formatFileSize(context, file.entry.size()!!.toLong())
    }
}
