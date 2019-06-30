package com.github.pockethub.android.ui.item

import com.github.pockethub.android.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class LoadingItem : Item() {

    override fun getLayout() = R.layout.loading_item

    override fun bind(holder: ViewHolder, position: Int) {}
}
