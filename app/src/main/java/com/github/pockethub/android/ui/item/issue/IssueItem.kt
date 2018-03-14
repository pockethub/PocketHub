package com.github.pockethub.android.ui.item.issue

import android.graphics.Color
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueUtils
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.IssueState
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.issue_details.view.*
import kotlinx.android.synthetic.main.issue_number.view.*
import kotlinx.android.synthetic.main.repo_issue_item.view.*

open class IssueItem @JvmOverloads constructor(private val avatarLoader: AvatarLoader, val issue: Issue, private val showLabels: Boolean = true) : Item<IssueItem.ItemViewHolder>(issue.id()!!) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        val labels = issue.labels()
        if (showLabels && labels != null && labels.isNotEmpty()) {
            holder.labels.forEachIndexed { index, view ->
                if (index >= 0 && index < labels.size) {
                    val label = labels[index]
                    if (!label.color().isNullOrEmpty()) {
                        view.setBackgroundColor(Color.parseColor('#' + label.color()!!))
                        view.visibility = View.VISIBLE
                        return@forEachIndexed
                    }
                }

                view.visibility = View.GONE
            }
        } else {
            holder.labels.forEach { it.visibility = View.GONE }
        }

        val numberText = StyledText()
                .append(issue.number().toString())
        if (IssueState.Closed == issue.state()) {
            numberText.strikethroughAll()
        }

        holder.root.tv_issue_number.text = numberText

        avatarLoader.bind(holder.root.iv_avatar, issue.user())

        if (IssueUtils.isPullRequest(issue)) {
            holder.root.tv_pull_request_icon.visibility = View.VISIBLE
        } else {
            holder.root.tv_pull_request_icon.visibility = View.GONE
        }

        holder.root.tv_issue_title.text = issue.title()
        holder.root.tv_issue_comments.text = issue.comments().toString()

        holder.root.tv_issue_creation.text = StyledText()
                .bold(issue.user()!!.login())
                .append(' ')
                .append(issue.createdAt())
    }

    override fun getLayout() = R.layout.repo_issue_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView) {

        val labels = listOf(root.v_label0, root.v_label1, root.v_label2, root.v_label3, root.v_label4, root.v_label5, root.v_label6, root.v_label7)
    }
}
