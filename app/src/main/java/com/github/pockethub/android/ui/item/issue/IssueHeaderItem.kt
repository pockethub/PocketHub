package com.github.pockethub.android.ui.item.issue

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueUtils
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.issue.LabelDrawableSpan
import com.github.pockethub.android.ui.view.OcticonTextView.ICON_COMMIT
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.IssueState
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.issue_header.view.*
import kotlinx.android.synthetic.main.milestone.view.*

class IssueHeaderItem(private val avatarLoader: AvatarLoader, private val imageGetter: HttpImageGetter, private val context: Context, private val actionListener: OnIssueHeaderActionListener, val issue: Issue) : Item<IssueHeaderItem.ItemViewHolder>(issue.id()!!) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        holder.root.tv_issue_title.text = issue.title()

        val body = issue.bodyHtml()
        if (!body.isNullOrEmpty()) {
            imageGetter.bind(holder.root.tv_issue_body, body, issue.id())
        } else {
            holder.root.tv_issue_body.setText(R.string.no_description_given)
        }

        holder.root.tv_issue_author.text = issue.user()!!.login()

        val created = StyledText()
        created.append(context.getString(R.string.prefix_opened))
        created.append(issue.createdAt())
        holder.root.tv_issue_creation_date.text = created

        avatarLoader.bind(holder.root.iv_avatar, issue.user())

        if (IssueUtils.isPullRequest(issue) && issue.pullRequest()!!.commits() != null
                && issue.pullRequest()!!.commits()!! > 0) {
            holder.root.ll_issue_commits.visibility = VISIBLE

            holder.root.tv_commit_icon.text = ICON_COMMIT

            val commits = context.getString(R.string.pull_request_commits,
                    issue.pullRequest()!!.commits())
            holder.root.tv_pull_request_commits.text = commits
        } else {
            holder.root.ll_issue_commits.visibility = GONE
        }

        val open = IssueState.Open == issue.state()
        if (!open) {
            val text = StyledText()
            text.bold(context.getString(R.string.closed))
            val closedAt = issue.closedAt()
            if (closedAt != null) {
                text.append(' ').append(closedAt)
            }
            holder.root.tv_state.text = text
            holder.root.tv_state.visibility = VISIBLE
        } else {
            holder.root.tv_state.visibility = GONE
        }

        val assignee = issue.assignee()
        if (assignee != null) {
            val name = StyledText()
            name.bold(assignee.login())
            name.append(' ').append(context.getString(R.string.assigned))
            holder.root.tv_assignee_name.text = name
            holder.root.iv_assignee_avatar.visibility = VISIBLE
            avatarLoader.bind(holder.root.iv_assignee_avatar, assignee)
        } else {
            holder.root.iv_assignee_avatar.visibility = GONE
            holder.root.tv_assignee_name.setText(R.string.unassigned)
        }

        val labels = issue.labels()
        if (labels != null && labels.isNotEmpty()) {
            LabelDrawableSpan.setText(holder.root.tv_labels, labels)
            holder.root.tv_labels.visibility = VISIBLE
        } else {
            holder.root.tv_labels.visibility = GONE
        }

        if (issue.milestone() != null) {
            val milestone = issue.milestone()
            val milestoneLabel = StyledText()
            milestoneLabel.append(context.getString(R.string.milestone_prefix))
            milestoneLabel.append(' ')
            milestoneLabel.bold(milestone!!.title())
            holder.root.tv_milestone.text = milestoneLabel
            val closed = milestone.closedIssues()!!.toFloat()
            val total = closed + milestone.openIssues()!!
            if (total > 0) {
                (holder.root.v_closed.layoutParams as LinearLayout.LayoutParams).weight = closed / total

                holder.root.v_closed.visibility = VISIBLE
            } else {
                holder.root.v_closed.visibility = GONE
            }
            holder.root.ll_milestone.visibility = VISIBLE
        } else {
            holder.root.ll_milestone.visibility = GONE
        }

        holder.root.ll_issue_commits.setOnClickListener { _ -> actionListener.onCommitsClicked() }
        holder.root.tv_state.setOnClickListener { _ -> actionListener.onStateClicked() }
        holder.root.ll_milestone.setOnClickListener { _ -> actionListener.onMilestonesClicked() }
        holder.root.ll_assignee.setOnClickListener { _ -> actionListener.onAssigneesClicked() }
        holder.root.tv_labels.setOnClickListener { _ -> actionListener.onLabelsClicked() }
    }

    override fun getLayout() = R.layout.issue_header

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView) {

        init {
            root.tv_issue_body.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    interface OnIssueHeaderActionListener {

        fun onCommitsClicked()

        fun onStateClicked()

        fun onMilestonesClicked()

        fun onAssigneesClicked()

        fun onLabelsClicked()
    }
}
