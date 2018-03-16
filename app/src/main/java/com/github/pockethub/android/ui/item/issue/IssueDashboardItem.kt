package com.github.pockethub.android.ui.item.issue

import android.widget.TextView

import com.github.pockethub.android.R
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.InfoUtils
import com.meisolsson.githubsdk.model.Issue

class IssueDashboardItem(avatarLoader: AvatarLoader, dataItem: Issue) : IssueItem(avatarLoader, dataItem) {

    override fun getLayout() = R.layout.dashboard_issue_item

    override fun bind(holder: IssueItem.ViewHolder, position: Int) {
        super.bind(holder, position)
        val textView = holder.root.findViewById<TextView>(R.id.tv_issue_repo_name)
        textView.text = InfoUtils.createRepoId(data.repository())
    }
}
