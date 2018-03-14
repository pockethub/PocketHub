package com.github.pockethub.android.ui.item.commit

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
import com.meisolsson.githubsdk.model.GitHubFile
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.commit_diff_file_header.view.*
import java.text.NumberFormat

class CommitFileHeaderItem(context: Context, val file: GitHubFile) : Item<CommitFileHeaderItem.ItemViewHolder>(file.sha()!!.hashCode().toLong()) {

    private val addTextColor = ContextCompat.getColor(context, R.color.diff_add_text)
    private val removeTextColor = ContextCompat.getColor(context, R.color.diff_remove_text)

    override fun bind(holder: ItemViewHolder, position: Int) {
        val path = file.filename()

        val lastSlash = path!!.lastIndexOf('/')
        if (lastSlash != -1) {
            holder.root.tv_name.text = path.substring(lastSlash + 1)
            holder.root.tv_folder.text = path.substring(0, lastSlash + 1)
            holder.root.tv_folder.visibility = View.VISIBLE
        } else {
            holder.root.tv_name.text = path
            holder.root.tv_folder.visibility = View.GONE
        }

        val numberFormat = NumberFormat.getIntegerInstance()

        holder.root.tv_stats.text = StyledText()
                .foreground('+', addTextColor)
                .foreground(numberFormat.format(file.additions()), addTextColor)
                .append(' ')
                .append(' ')
                .append(' ')
                .foreground('-', removeTextColor)
                .foreground(numberFormat.format(file.deletions()), removeTextColor)
    }

    override fun getLayout() = R.layout.commit_diff_file_header

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
