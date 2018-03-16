package com.github.pockethub.android.ui.item.issue

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.BindViews
import butterknife.ButterKnife
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueUtils
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ButterKnifeUtils
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.IssueState
import com.meisolsson.githubsdk.model.Label

open class IssueItem @JvmOverloads constructor(avatarLoader: AvatarLoader, dataItem: Issue, private val showLabels: Boolean = true) : BaseDataItem<Issue, IssueItem.ViewHolder>(avatarLoader, dataItem, dataItem.id()!!) {

    override fun getLayout() = R.layout.repo_issue_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val labels = data.labels()
        if (showLabels && labels != null && !labels.isEmpty()) {
            ButterKnife.apply(holder.labels, LABEL_SETTER, labels)
        } else {
            ButterKnife.apply(holder.labels, ButterKnifeUtils.GONE)
        }

        val numberText = StyledText()
        numberText.append(data.number().toString())
        if (IssueState.Closed == data.state()) {
            numberText.strikethroughAll()
        }

        holder.number.text = numberText

        avatarLoader.bind(holder.avatar, data.user())

        if (IssueUtils.isPullRequest(data)) {
            holder.pullRequest.visibility = View.VISIBLE
        } else {
            holder.pullRequest.visibility = View.GONE
        }

        holder.title.text = data.title()
        holder.comments.text = data.comments().toString()

        val reporterText = StyledText()
        reporterText.bold(data.user()!!.login())
        reporterText.append(' ')
        reporterText.append(data.createdAt())
        holder.creation.text = reporterText
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_issue_number)
        lateinit var number: TextView

        @BindView(R.id.tv_issue_title)
        lateinit var title: TextView

        @BindView(R.id.iv_avatar)
        lateinit var avatar: ImageView

        @BindView(R.id.tv_issue_creation)
        lateinit var creation: TextView

        @BindView(R.id.tv_issue_comments)
        lateinit var comments: TextView

        @BindView(R.id.tv_pull_request_icon)
        lateinit var pullRequest: TextView

        @BindViews(R.id.v_label0, R.id.v_label1, R.id.v_label2, R.id.v_label3, R.id.v_label4, R.id.v_label5, R.id.v_label6, R.id.v_label7)
        lateinit var labels: List<View>
    }

    companion object {

        private val LABEL_SETTER = ButterKnife.Setter<View, List<Label>> { view, labels, i ->
            if (i >= 0 && i < labels.size) {
                val label = labels[i]
                if (!TextUtils.isEmpty(label.color())) {
                    view.setBackgroundColor(Color.parseColor('#' + label.color()!!))
                    view.visibility = View.VISIBLE
                    return@Setter
                }
            }

            view.visibility = View.GONE
        }
    }
}
