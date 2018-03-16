package com.github.pockethub.android.ui.item.dialog

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.issue.LabelDrawableSpan
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.meisolsson.githubsdk.model.Label

class LabelDialogItem(dataItem: Label, selected: Boolean) : BaseDataItem<Label, LabelDialogItem.ViewHolder>(null, dataItem, dataItem.name()!!.hashCode().toLong()) {

    var isSelected: Boolean = selected
        private set

    override fun getLayout() = R.layout.label_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        LabelDrawableSpan.setText(holder.name, data)
        holder.selected.isChecked = isSelected
    }

    fun toggleSelected() {
        isSelected = !isSelected
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_label_name)
        lateinit var name: TextView

        @BindView(R.id.cb_selected)
        lateinit var selected: CheckBox
    }
}
