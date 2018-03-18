package com.github.pockethub.android.ui.item.issue

import android.widget.TextView

import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.InfoUtils
import com.meisolsson.githubsdk.model.Issue
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class IssueDashboardItem(
        avatarLoader: AvatarLoader,
        issue: Issue
) : IssueItem(avatarLoader, issue) {

    override fun getLayout() = R.layout.dashboard_issue_item

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        val textView = holder.root.findViewById<TextView>(R.id.tv_issue_repo_name)
        textView.text = InfoUtils.createRepoId(issue.repository())
    }
}
