package com.github.pockethub.android.ui.item.dialog

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.issue.LabelDrawableSpan
import com.meisolsson.githubsdk.model.Label
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.label_item.view.*

class LabelDialogItem(val label: Label, selected: Boolean) : Item<LabelDialogItem.ItemViewHolder>(label.name()!!.hashCode().toLong()) {

    var isSelected: Boolean = false
        private set

    init {
        this.isSelected = selected
    }

    fun toggleSelected() {
        isSelected = !isSelected
    }

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        LabelDrawableSpan.setText(viewHolder.root.tv_label_name, label)
        viewHolder.root.cb_selected.isChecked = isSelected
    }

    override fun getLayout() = R.layout.label_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
