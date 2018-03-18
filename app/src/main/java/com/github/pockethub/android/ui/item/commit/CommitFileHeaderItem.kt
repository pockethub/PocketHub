package com.github.pockethub.android.ui.item.commit

import android.content.Context
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
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

        val stats = StyledText()
        stats.foreground('+', addTextColor)
        stats.foreground(numberFormat.format(file.additions()), addTextColor)
        stats.append(' ').append(' ').append(' ')
        stats.foreground('-', removeTextColor)
        stats.foreground(numberFormat.format(file.deletions()), removeTextColor)
        holder.tv_stats.text = stats
    }
}
