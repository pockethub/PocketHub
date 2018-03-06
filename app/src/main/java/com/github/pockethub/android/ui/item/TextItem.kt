package com.github.pockethub.android.ui.item

import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.widget.TextView

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class TextItem(@param:LayoutRes private val layoutId: Int, @param:IdRes private val textViewId: Int, private val text: CharSequence) : Item<ViewHolder>(text.hashCode().toLong()) {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val textView = viewHolder.root.findViewById<TextView>(textViewId)
        textView.text = text
    }

    override fun getLayout() = layoutId
}
