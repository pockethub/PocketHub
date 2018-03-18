package com.github.pockethub.android.ui.item.dialog

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.R
import com.meisolsson.githubsdk.model.Milestone
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.milestone_item.*

class MilestoneDialogItem(
        val milestone: Milestone,
        private val selected: Int
) : Item(milestone.id()!!) {

    override fun getLayout() = R.layout.milestone_item

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_milestone_title.text = milestone.title()

        val description = milestone.description()
        if (!TextUtils.isEmpty(description)) {
            holder.tv_milestone_description.text = description
            holder.tv_milestone_description.visibility = View.VISIBLE
        } else {
            holder.tv_milestone_description.visibility = View.GONE
        }

        holder.rb_selected.isChecked = selected == position
    }
}
