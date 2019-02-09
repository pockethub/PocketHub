package com.github.pockethub.android.ui.item.commit

import android.content.Context
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.github.pockethub.android.R
import com.meisolsson.githubsdk.model.GitHubFile
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.commit_diff_file_header.*
import java.text.NumberFormat

class CommitFileHeaderItem(
        context: Context,
        val file: GitHubFile
) : Item(file.sha()!!.hashCode().toLong()) {

    private val addTextColor = context.resources.getColor(R.color.diff_add_text)
    private val removeTextColor = context.resources.getColor(R.color.diff_remove_text)

    override fun getLayout() = R.layout.commit_diff_file_header

    override fun bind(holder: ViewHolder, position: Int) {
        val path = file.filename()

        val lastSlash = path!!.lastIndexOf('/')
        if (lastSlash != -1) {
            holder.tv_name.text = path.substring(lastSlash + 1)
            holder.tv_folder.text = path.substring(0, lastSlash + 1)
            holder.tv_folder.visibility = View.VISIBLE
        } else {
            holder.tv_name.text = path
            holder.tv_folder.visibility = View.GONE
        }

        val numberFormat = NumberFormat.getIntegerInstance()

        holder.tv_stats.text = buildSpannedString {
            color(addTextColor) {
                append("+${numberFormat.format(file.additions())}")
            }
            append("   ")
            color(removeTextColor) {
                append("-${numberFormat.format(file.deletions())}")
            }
        }
    }
}
