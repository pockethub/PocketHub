package com.github.pockethub.android.ui.item

import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import android.widget.TextView
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class TextItem(
        @param:LayoutRes private val layoutId: Int,
        @param:IdRes private val textViewId: Int,
        private val text: CharSequence
) : Item(text.hashCode().toLong()) {

    override fun getLayout() = layoutId

    override fun bind(holder: ViewHolder, position: Int) {
        val textView = holder.root.findViewById<TextView>(textViewId)
        textView.text = text
    }
}
