package com.github.pockethub.android.ui.item.code

import com.github.pockethub.android.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.path_item.*

class PathHeaderItem(private val path: CharSequence) : Item(path.hashCode().toLong()) {

    override fun getLayout() = R.layout.path_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_path.text = path
    }
}
