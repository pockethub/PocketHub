package com.github.pockethub.android.ui.item.dialog

import com.github.pockethub.android.R
import com.github.pockethub.android.ui.issue.LabelDrawableSpan
import com.meisolsson.githubsdk.model.Label
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.label_item.*

class LabelDialogItem(
        val label: Label,
        selected: Boolean
) : Item(label.name()!!.hashCode().toLong()) {

    var isSelected: Boolean = selected
        private set

    override fun getLayout() = R.layout.label_item

    override fun bind(holder: ViewHolder, position: Int) {
        LabelDrawableSpan.setText(holder.tv_label_name, label)
        holder.cb_selected.isChecked = isSelected
    }

    fun toggleSelected() {
        isSelected = !isSelected
    }
}
