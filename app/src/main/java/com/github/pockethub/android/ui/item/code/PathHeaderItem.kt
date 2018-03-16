package com.github.pockethub.android.ui.item.code

import android.view.View
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.ui.view.LinkTextView
import com.xwray.groupie.Item

class PathHeaderItem(private val path: CharSequence) : Item<PathHeaderItem.ViewHolder>(path.hashCode().toLong()) {

    override fun getLayout() = R.layout.path_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        holder.path.text = path
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_path)
        lateinit var path: LinkTextView
    }
}
