package com.github.pockethub.android.ui.item.commit

import android.content.Context
import com.github.pockethub.android.R
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.ui.StyledText
import com.meisolsson.githubsdk.model.Commit
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.commit_parent_item.*

class CommitParentItem(
        private val context: Context,
        val commit: Commit
) : Item(commit.sha()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.commit_parent_item

    override fun bind(holder: ViewHolder, position: Int) {
        val parentText = StyledText()
                .append(context.getString(R.string.parent_prefix))
                .monospace(CommitUtils.abbreviate(commit.sha()))
                .underlineAll()
        holder.tv_commit_id.text = parentText
    }
}
