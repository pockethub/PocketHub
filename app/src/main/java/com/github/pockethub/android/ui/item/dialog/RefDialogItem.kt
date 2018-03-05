package com.github.pockethub.android.ui.item.dialog

import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.core.ref.RefUtils
import com.meisolsson.githubsdk.model.git.GitReference
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ref_item.view.*

class RefDialogItem(val gitReference: GitReference, private val selected: Int) : Item<RefDialogItem.ItemViewHolder>(gitReference.ref()!!.hashCode().toLong()) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        if (RefUtils.isTag(gitReference)) {
            holder.root.tv_ref_icon.setText(R.string.icon_tag)
        } else {
            holder.root.tv_ref_icon.setText(R.string.icon_fork)
        }
        holder.root.tv_ref.text = RefUtils.getName(gitReference)
        holder.root.rb_selected.isChecked = selected == position
    }

    override fun getLayout() = R.layout.ref_item

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView)
}
