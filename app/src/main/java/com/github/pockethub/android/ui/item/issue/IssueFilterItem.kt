package com.github.pockethub.android.ui.item.issue

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueFilter
import com.github.pockethub.android.ui.issue.LabelDrawableSpan
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.InfoUtils

class IssueFilterItem(avatarLoader: AvatarLoader, issueFilter: IssueFilter) : BaseDataItem<IssueFilter, IssueFilterItem.ViewHolder>(avatarLoader, issueFilter, issueFilter.hashCode().toLong()) {

    override fun getLayout() = R.layout.issues_filter_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        avatarLoader.bind(holder.avatar, data.repository.owner())

        holder.repoName.text = InfoUtils.createRepoId(data.repository)

        if (data.isOpen) {
            holder.filterState.setText(R.string.open_issues)
        } else {
            holder.filterState.setText(R.string.closed_issues)
        }

        val labels = data.labels
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(holder.filterLabels, labels)
            holder.filterLabels.visibility = View.VISIBLE
        } else {
            holder.filterLabels.visibility = View.GONE
        }

        val milestone = data.milestone
        if (milestone != null) {
            holder.filterMilestone.text = milestone.title()
            holder.filterMilestone.visibility = View.VISIBLE
        } else {
            holder.filterMilestone.visibility = View.GONE
        }

        val assignee = data.assignee
        if (assignee != null) {
            avatarLoader.bind(holder.assigneeAvatar, assignee)
            holder.filterAssignee.text = assignee.login()
            holder.filterAssignee.visibility = View.VISIBLE
        } else {
            holder.assignee.visibility = View.GONE
        }
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.iv_avatar)
        lateinit var avatar: ImageView

        @BindView(R.id.tv_repo_name)
        lateinit var repoName: TextView

        @BindView(R.id.tv_filter_state)
        lateinit var filterState: TextView

        @BindView(R.id.tv_filter_labels)
        lateinit var filterLabels: TextView

        @BindView(R.id.tv_filter_milestone)
        lateinit var filterMilestone: TextView

        @BindView(R.id.ll_assignee)
        lateinit var assignee: LinearLayout

        @BindView(R.id.tv_filter_assignee)
        lateinit var filterAssignee: TextView

        @BindView(R.id.iv_assignee_avatar)
        lateinit var assigneeAvatar: ImageView
    }
}
