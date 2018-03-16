package com.github.pockethub.android.ui.item.commit

import android.content.Context
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.meisolsson.githubsdk.model.Commit

class CommitParentItem(private val context: Context, commit: Commit) : BaseDataItem<Commit, CommitParentItem.ViewHolder>(null, commit, commit.sha()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.commit_parent_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val parentText = StyledText()
                .append(context.getString(R.string.parent_prefix))
                .monospace(CommitUtils.abbreviate(data.sha()))
                .underlineAll()
        holder.commitId.text = parentText
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_commit_id)
        lateinit var commitId: TextView
    }
}
