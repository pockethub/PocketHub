package com.github.pockethub.android.ui.item

import android.support.annotation.StringRes
import android.view.View

import com.github.pockethub.android.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

import kotlinx.android.synthetic.main.loading_item.view.*

class LoadingItem(@param:StringRes private val loadingResId: Int) : Item<LoadingItem.ItemViewHolder>() {

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_loading.setText(loadingResId)
    }

    override fun getLayout() = R.layout.loading_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
