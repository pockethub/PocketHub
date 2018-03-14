package com.github.pockethub.android.ui.item.dialog

import android.view.View
import com.github.pockethub.android.R
import com.meisolsson.githubsdk.model.Milestone
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.milestone_item.view.*

class MilestoneDialogItem(val milestone: Milestone, private val selected: Int) : Item<MilestoneDialogItem.ItemViewHolder>(milestone.id()!!) {

    override fun bind(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.root.tv_milestone_title.text = milestone.title()

        val description = milestone.description()
        if (!description.isNullOrEmpty()) {
            viewHolder.root.tv_milestone_description.text = description
            viewHolder.root.tv_milestone_description.visibility = View.VISIBLE
        } else {
            viewHolder.root.tv_milestone_description.visibility = View.GONE
        }

        viewHolder.root.rb_selected.isChecked = selected == position
    }

    override fun getLayout() = R.layout.milestone_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
