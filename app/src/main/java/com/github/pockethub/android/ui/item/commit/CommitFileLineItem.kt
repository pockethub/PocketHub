package com.github.pockethub.android.ui.item.commit

import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.commit.DiffStyler
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder

class CommitFileLineItem(private val diffStyler: DiffStyler, line: CharSequence) : BaseDataItem<CharSequence, CommitFileLineItem.ViewHolder>(null, line, line.hashCode().toLong()) {

    override fun getLayout() = R.layout.commit_diff_line

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        holder.diff.text = data
        diffStyler.updateColors(data, holder.diff)
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_diff)
        lateinit var diff: TextView
    }
}
