package com.github.pockethub.android.ui.item.commit

import com.github.pockethub.android.R
import com.github.pockethub.android.ui.commit.DiffStyler
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.commit_diff_line.*

class CommitFileLineItem(
        private val diffStyler: DiffStyler,
        val line: CharSequence
) : Item(line.hashCode().toLong()) {

    override fun getLayout() = R.layout.commit_diff_line

    override fun bind(holder: ViewHolder, position: Int) {
        holder.tv_diff.text = line
        diffStyler.updateColors(line, holder.tv_diff)
    }
}
