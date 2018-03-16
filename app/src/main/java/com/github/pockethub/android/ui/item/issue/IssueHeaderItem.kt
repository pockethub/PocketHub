package com.github.pockethub.android.ui.item.issue

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueUtils
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.issue.LabelDrawableSpan
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.ui.view.LinkTextView
import com.github.pockethub.android.ui.view.OcticonTextView.ICON_COMMIT
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.IssueState

class IssueHeaderItem(avatarLoader: AvatarLoader, private val imageGetter: HttpImageGetter, private val context: Context, private val actionListener: OnIssueHeaderActionListener, dataItem: Issue) : BaseDataItem<Issue, IssueHeaderItem.ViewHolder>(avatarLoader, dataItem, dataItem.id()!!) {

    override fun bind(holder: ViewHolder, position: Int) {
        val issue = data
        holder.title.text = issue.title()

        val body = issue.bodyHtml()
        if (!TextUtils.isEmpty(body)) {
            imageGetter.bind(holder.bodyText, body, issue.id())
        } else {
            holder.bodyText.setText(R.string.no_description_given)
        }

        holder.author.text = issue.user()!!.login()

        val created = StyledText()
        created.append(context.getString(R.string.prefix_opened))
        created.append(issue.createdAt())
        holder.created.text = created

        avatarLoader.bind(holder.avatar, issue.user())

        if (IssueUtils.isPullRequest(issue) && issue.pullRequest()!!.commits() != null && issue.pullRequest()!!.commits()!! > 0) {
            holder.commitsView.visibility = VISIBLE

            holder.commitIcon.text = ICON_COMMIT

            val commits = context.getString(R.string.pull_request_commits, issue.pullRequest()!!.commits())
            holder.pullRequestCommits.text = commits
        } else {
            holder.commitsView.visibility = GONE
        }

        val open = IssueState.Open == issue.state()
        if (!open) {
            val text = StyledText()
            text.bold(context.getString(R.string.closed))
            val closedAt = issue.closedAt()
            if (closedAt != null) {
                text.append(' ').append(closedAt)
            }
            holder.state.text = text
            holder.state.visibility = VISIBLE
        } else {
            holder.state.visibility = GONE
        }

        val assignee = issue.assignee()
        if (assignee != null) {
            val name = StyledText()
            name.bold(assignee.login())
            name.append(' ').append(context.getString(R.string.assigned))
            holder.assigneeName.text = name
            holder.assigneeAvatar.visibility = VISIBLE
            avatarLoader.bind(holder.assigneeAvatar, assignee)
        } else {
            holder.assigneeAvatar.visibility = GONE
            holder.assigneeName.setText(R.string.unassigned)
        }

        val labels = issue.labels()
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(holder.labels, labels)
            holder.labels.visibility = VISIBLE
        } else {
            holder.labels.visibility = GONE
        }

        if (issue.milestone() != null) {
            val milestone = issue.milestone()
            val milestoneLabel = StyledText()
            milestoneLabel.append(context.getString(R.string.milestone_prefix))
            milestoneLabel.append(' ')
            milestoneLabel.bold(milestone!!.title())
            holder.milestoneText.text = milestoneLabel
            val closed = milestone.closedIssues()!!.toFloat()
            val total = closed + milestone.openIssues()!!
            if (total > 0) {
                (holder.milestoneProgressArea.layoutParams as LinearLayout.LayoutParams).weight = closed / total

                holder.milestoneProgressArea.visibility = VISIBLE
            } else {
                holder.milestoneProgressArea.visibility = GONE
            }
            holder.milestoneArea.visibility = VISIBLE
        } else {
            holder.milestoneArea.visibility = GONE
        }

        holder.commitsView.setOnClickListener { _ -> actionListener.onCommitsClicked() }
        holder.state.setOnClickListener { _ -> actionListener.onStateClicked() }
        holder.milestoneArea.setOnClickListener { _ -> actionListener.onMilestonesClicked() }
        holder.assigneeArea.setOnClickListener { _ -> actionListener.onAssigneesClicked() }
        holder.labels.setOnClickListener { _ -> actionListener.onLabelsClicked() }

    }

    override fun getLayout(): Int {
        return R.layout.issue_header
    }

    override fun createViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_state)
        lateinit var state: TextView

        @BindView(R.id.tv_issue_title)
        lateinit var title: TextView

        @BindView(R.id.tv_issue_author)
        lateinit var author: TextView

        @BindView(R.id.tv_issue_creation_date)
        lateinit var created: TextView

        @BindView(R.id.iv_avatar)
        lateinit var avatar: ImageView

        @BindView(R.id.ll_assignee)
        lateinit var assigneeArea: LinearLayout

        @BindView(R.id.tv_assignee_name)
        lateinit var assigneeName: TextView

        @BindView(R.id.iv_assignee_avatar)
        lateinit var assigneeAvatar: ImageView

        @BindView(R.id.tv_labels)
        lateinit var labels: TextView

        @BindView(R.id.ll_issue_commits)
        lateinit var commitsView: LinearLayout

        @BindView(R.id.tv_commit_icon)
        lateinit var commitIcon: TextView

        @BindView(R.id.tv_pull_request_commits)
        lateinit var pullRequestCommits: TextView

        @BindView(R.id.ll_milestone)
        lateinit var milestoneArea: LinearLayout

        @BindView(R.id.tv_milestone)
        lateinit var milestoneText: TextView

        @BindView(R.id.v_closed)
        lateinit var milestoneProgressArea: View

        @BindView(R.id.tv_issue_body)
        lateinit var bodyText: LinkTextView
    }

    interface OnIssueHeaderActionListener {

        fun onCommitsClicked()

        fun onStateClicked()

        fun onMilestonesClicked()

        fun onAssigneesClicked()

        fun onLabelsClicked()
    }
}
