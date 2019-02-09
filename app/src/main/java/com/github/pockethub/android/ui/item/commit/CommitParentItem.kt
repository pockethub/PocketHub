package com.github.pockethub.android.ui.item.commit

import android.content.Context
import androidx.core.text.buildSpannedString
import androidx.core.text.underline
import com.github.pockethub.android.R
import com.github.pockethub.android.util.android.text.monospace
import com.github.pockethub.android.core.commit.CommitUtils
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
        holder.tv_commit_id.text = buildSpannedString {
            underline {
                append(context.getString(R.string.parent_prefix))
                monospace {
                    append(CommitUtils.abbreviate(commit.sha()))
                }
            }
        }
    }
}
