package com.github.pockethub.android.ui.item.repository

import com.github.pockethub.android.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.repo_header_item.*

class RepositoryHeaderItem(private val id: String) : Item(id.hashCode().toLong()) {

    override fun getLayout() = R.layout.repo_header_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_header.text = id
    }

    override fun isClickable() = false

    override fun isLongClickable() = false
}
