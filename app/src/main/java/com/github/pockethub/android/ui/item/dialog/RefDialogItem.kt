package com.github.pockethub.android.ui.item.dialog

import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.core.ref.RefUtils
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.meisolsson.githubsdk.model.git.GitReference

class RefDialogItem(dataItem: GitReference, private val selected: Int) : BaseDataItem<GitReference, RefDialogItem.ViewHolder>(null, dataItem, dataItem.ref()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.ref_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        if (RefUtils.isTag(data)) {
            holder.refIcon.setText(R.string.icon_tag)
        } else {
            holder.refIcon.setText(R.string.icon_fork)
        }
        holder.ref.text = RefUtils.getName(data)
        holder.selected.isChecked = selected == position
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_ref_icon)
        lateinit var refIcon: TextView

        @BindView(R.id.tv_ref)
        lateinit var ref: TextView

        @BindView(R.id.rb_selected)
        lateinit var selected: RadioButton
    }
}
