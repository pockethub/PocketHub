package com.github.pockethub.android.ui.item.issue

import android.content.Context
import android.text.TextUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.R
import com.github.pockethub.android.util.android.text.append
import com.github.pockethub.android.core.issue.IssueUtils
import com.github.pockethub.android.ui.issue.LabelDrawableSpan
import com.github.pockethub.android.ui.view.OcticonTextView.ICON_COMMIT
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.IssueState
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.issue_header.*
import kotlinx.android.synthetic.main.milestone.*

class IssueHeaderItem(
        private val avatarLoader: AvatarLoader,
        private val imageGetter: HttpImageGetter,
        private val context: Context,
        private val actionListener: OnIssueHeaderActionListener,
        val issue: Issue
) : Item(issue.id()!!) {

    override fun getLayout() = R.layout.issue_header

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_issue_title.text = issue.title()

        val body = issue.bodyHtml()
        if (!TextUtils.isEmpty(body)) {
            imageGetter.bind(holder.tv_issue_body, body, issue.id())
        } else {
            holder.tv_issue_body.setText(R.string.no_description_given)
        }

        holder.tv_issue_author.text = issue.user()!!.login()

        holder.tv_issue_creation_date.text = buildSpannedString {
            append("${context.getString(R.string.prefix_opened)}${issue.createdAt()}")
        }

        avatarLoader.bind(holder.iv_avatar, issue.user())

        if (IssueUtils.isPullRequest(issue) &&
                issue.pullRequest()!!.commits() != null &&
                issue.pullRequest()!!.commits()!! > 0
        ) {
            holder.ll_issue_commits.visibility = VISIBLE

            holder.tv_commit_icon.text = ICON_COMMIT

            val commits = context.getString(
                    R.string.pull_request_commits,
                    issue.pullRequest()!!.commits()
            )
            holder.tv_pull_request_commits.text = commits
        } else {
            holder.ll_issue_commits.visibility = GONE
        }

        val open = IssueState.Open == issue.state()
        if (!open) {
            holder.tv_state.text = buildSpannedString {
                bold {
                    append(context.getString(R.string.closed))
                }
                val closedAt = issue.closedAt()
                if (closedAt != null) {
                    append(' ')
                    append(closedAt)
                }
            }
            holder.tv_state.visibility = VISIBLE
        } else {
            holder.tv_state.visibility = GONE
        }

        val assignee = issue.assignee()
        if (assignee != null) {
            holder.tv_assignee_name.text = buildSpannedString {
                bold {
                    append(assignee.login())
                }
                append(" ${context.getString(R.string.assigned)}")
            }
            holder.iv_assignee_avatar.visibility = VISIBLE
            avatarLoader.bind(holder.iv_assignee_avatar, assignee)
        } else {
            holder.iv_assignee_avatar.visibility = GONE
            holder.tv_assignee_name.setText(R.string.unassigned)
        }

        val labels = issue.labels()
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(holder.tv_labels, labels)
            holder.tv_labels.visibility = VISIBLE
        } else {
            holder.tv_labels.visibility = GONE
        }

        if (issue.milestone() != null) {
            val milestone = issue.milestone()
            holder.tv_milestone.text = buildSpannedString {
                append("${context.getString(R.string.milestone_prefix)} ")
                bold {
                    append(milestone!!.title())
                }
            }
            val closed = milestone!!.closedIssues()!!.toFloat()
            val total = closed + milestone.openIssues()!!
            if (total > 0) {
                (holder.v_closed.layoutParams as LinearLayout.LayoutParams).weight = closed / total

                holder.v_closed.visibility = VISIBLE
            } else {
                holder.v_closed.visibility = GONE
            }
            holder.ll_milestone.visibility = VISIBLE
        } else {
            holder.ll_milestone.visibility = GONE
        }

        holder.ll_issue_commits.setOnClickListener { _ -> actionListener.onCommitsClicked() }
        holder.tv_state.setOnClickListener { _ -> actionListener.onStateClicked() }
        holder.ll_milestone.setOnClickListener { _ -> actionListener.onMilestonesClicked() }
        holder.ll_assignee.setOnClickListener { _ -> actionListener.onAssigneesClicked() }
        holder.tv_labels.setOnClickListener { _ -> actionListener.onLabelsClicked() }

    }

    interface OnIssueHeaderActionListener {

        fun onCommitsClicked()

        fun onStateClicked()

        fun onMilestonesClicked()

        fun onAssigneesClicked()

        fun onLabelsClicked()
    }
}
