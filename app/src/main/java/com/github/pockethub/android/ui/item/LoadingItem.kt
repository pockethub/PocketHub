package com.github.pockethub.android.ui.item

import androidx.annotation.StringRes
import com.github.pockethub.android.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.loading_item.*

class LoadingItem(@param:StringRes private val loadingResId: Int) : Item() {

    override fun getLayout() = R.layout.loading_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_loading.setText(loadingResId)
    }
}
