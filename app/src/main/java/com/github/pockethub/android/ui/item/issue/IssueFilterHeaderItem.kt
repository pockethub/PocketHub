package com.github.pockethub.android.ui.item.issue

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueFilter
import com.github.pockethub.android.ui.issue.LabelDrawableSpan
import com.github.pockethub.android.util.AvatarLoader
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.issues_filter_details.view.*

class IssueFilterHeaderItem(private val avatarLoader: AvatarLoader, val issueFilter: IssueFilter) : Item<IssueFilterHeaderItem.ItemViewHolder>(issueFilter.hashCode().toLong()) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        if (issueFilter.isOpen) {
            holder.root.tv_filter_state.setText(R.string.open_issues)
        } else {
            holder.root.tv_filter_state.setText(R.string.closed_issues)
        }

        val labels = issueFilter.labels
        if (labels != null && labels.isNotEmpty()) {
            LabelDrawableSpan.setText(holder.root.tv_filter_labels, labels)
            holder.root.tv_filter_labels.visibility = View.VISIBLE
        } else {
            holder.root.tv_filter_labels.visibility = View.GONE
        }

        val milestone = issueFilter.milestone
        if (milestone != null) {
            holder.root.tv_filter_milestone.text = milestone.title()
            holder.root.tv_filter_milestone.visibility = View.VISIBLE
        } else {
            holder.root.tv_filter_milestone.visibility = View.GONE
        }

        val assignee = issueFilter.assignee
        if (assignee != null) {
            avatarLoader.bind(holder.root.iv_assignee_avatar, assignee)
            holder.root.tv_filter_assignee.text = assignee.login()
            holder.root.tv_filter_assignee.visibility = View.VISIBLE
        } else {
            holder.root.ll_assignee.visibility = View.GONE
        }
    }

    override fun getLayout() = R.layout.issues_filter_header

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
