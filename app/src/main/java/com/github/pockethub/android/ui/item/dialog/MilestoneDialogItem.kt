package com.github.pockethub.android.ui.item.dialog

import android.text.TextUtils
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.meisolsson.githubsdk.model.Milestone

class MilestoneDialogItem(dataItem: Milestone, private val selected: Int) : BaseDataItem<Milestone, MilestoneDialogItem.ViewHolder>(null, dataItem, dataItem.id()!!) {

    override fun getLayout() = R.layout.milestone_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        holder.title.text = data.title()

        val description = data.description()
        if (!TextUtils.isEmpty(description)) {
            holder.description.text = description
            holder.description.visibility = View.VISIBLE
        } else {
            holder.description.visibility = View.GONE
        }

        holder.selected.isChecked = selected == position
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.rb_selected)
        lateinit var selected: RadioButton

        @BindView(R.id.tv_milestone_title)
        lateinit var title: TextView

        @BindView(R.id.tv_milestone_description)
        lateinit var description: TextView
    }
}
