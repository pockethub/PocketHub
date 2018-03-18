package com.github.pockethub.android.ui.item.issue

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueFilter
import com.github.pockethub.android.ui.issue.LabelDrawableSpan
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.InfoUtils
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.issues_filter_details.*
import kotlinx.android.synthetic.main.issues_filter_item.*

class IssueFilterItem(
        private val avatarLoader: AvatarLoader,
        val issueFilter: IssueFilter
) : Item(issueFilter.hashCode().toLong()) {

    override fun getLayout() = R.layout.issues_filter_item

    override fun bind(holder: ViewHolder, position: Int) {
        avatarLoader.bind(holder.iv_avatar, issueFilter.repository.owner())

        holder.tv_repo_name.text = InfoUtils.createRepoId(issueFilter.repository)

        if (issueFilter.isOpen) {
            holder.tv_filter_state.setText(R.string.open_issues)
        } else {
            holder.tv_filter_state.setText(R.string.closed_issues)
        }

        val labels = issueFilter.labels
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(holder.tv_filter_labels, labels)
            holder.tv_filter_labels.visibility = View.VISIBLE
        } else {
            holder.tv_filter_labels.visibility = View.GONE
        }

        val milestone = issueFilter.milestone
        if (milestone != null) {
            holder.tv_filter_milestone.text = milestone.title()
            holder.tv_filter_milestone.visibility = View.VISIBLE
        } else {
            holder.tv_filter_milestone.visibility = View.GONE
        }

        val assignee = issueFilter.assignee
        if (assignee != null) {
            avatarLoader.bind(holder.iv_assignee_avatar, assignee)
            holder.tv_filter_assignee.text = assignee.login()
            holder.tv_filter_assignee.visibility = View.VISIBLE
        } else {
            holder.ll_assignee.visibility = View.GONE
        }
    }
}
