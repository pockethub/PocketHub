package com.github.pockethub.android.ui.item.dialog

import com.github.pockethub.android.R
import com.github.pockethub.android.core.ref.RefUtils
import com.meisolsson.githubsdk.model.git.GitReference
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.ref_item.*

class RefDialogItem(
        val gitReference: GitReference,
        private val selected: Int
) : Item(gitReference.ref()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.ref_item

    override fun bind(holder: ViewHolder, position: Int) {
        if (RefUtils.isTag(gitReference)) {
            holder.tv_ref_icon.setText(R.string.icon_tag)
        } else {
            holder.tv_ref_icon.setText(R.string.icon_fork)
        }
        holder.tv_ref.text = RefUtils.getName(gitReference)
        holder.rb_selected.isChecked = selected == position
    }
}
