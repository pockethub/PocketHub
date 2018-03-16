package com.github.pockethub.android.ui.item.commit

import android.content.Context
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.meisolsson.githubsdk.model.GitHubFile
import java.text.NumberFormat

class CommitFileHeaderItem(context: Context, file: GitHubFile) : BaseDataItem<GitHubFile, CommitFileHeaderItem.ViewHolder>(null, file, file.sha()!!.hashCode().toLong()) {

    private val addTextColor = context.resources.getColor(R.color.diff_add_text)
    private val removeTextColor = context.resources.getColor(R.color.diff_remove_text)

    override fun getLayout() = R.layout.commit_diff_file_header

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val file = data
        val path = file.filename()

        val lastSlash = path!!.lastIndexOf('/')
        if (lastSlash != -1) {
            holder.filename.text = path.substring(lastSlash + 1)
            holder.folder.text = path.substring(0, lastSlash + 1)
            holder.folder.visibility = View.VISIBLE
        } else {
            holder.filename.text = path
            holder.folder.visibility = View.GONE
        }

        val numberFormat = NumberFormat.getIntegerInstance()

        val stats = StyledText()
        stats.foreground('+', addTextColor)
        stats.foreground(numberFormat.format(file.additions()), addTextColor)
        stats.append(' ').append(' ').append(' ')
        stats.foreground('-', removeTextColor)
        stats.foreground(numberFormat.format(file.deletions()), removeTextColor)
        holder.stats.text = stats
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_name)
        lateinit var filename: TextView

        @BindView(R.id.tv_folder)
        lateinit var folder: TextView

        @BindView(R.id.tv_stats)
        lateinit var stats: TextView
    }
}
