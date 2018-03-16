package com.github.pockethub.android.ui.item

import android.support.annotation.StringRes
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.xwray.groupie.Item

class LoadingItem(@param:StringRes private val loadingResId: Int) : Item<LoadingItem.ViewHolder>() {

    override fun getLayout() = R.layout.loading_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        holder.loading.setText(loadingResId)
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_loading)
        lateinit var loading: TextView
    }
}
