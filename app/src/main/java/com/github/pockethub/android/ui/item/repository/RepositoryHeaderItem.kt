package com.github.pockethub.android.ui.item.repository

import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.xwray.groupie.Item

class RepositoryHeaderItem(private val id: String) : Item<RepositoryHeaderItem.ViewHolder>(id.hashCode().toLong()) {

    override fun getLayout() = R.layout.repo_header_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        holder.header.text = id
    }

    override fun isClickable() = false

    override fun isLongClickable() = false

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_header)
        lateinit var header: TextView
    }
}
