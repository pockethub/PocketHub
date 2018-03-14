package com.github.pockethub.android.ui.item.gist

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
import com.meisolsson.githubsdk.model.Gist
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.gist_header.view.*

class GistHeaderItem(private val context: Context, val gist: Gist) : Item<GistHeaderItem.ItemViewHolder>(gist.id()!!.hashCode().toLong()) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        val createdAt = gist.createdAt()
        if (createdAt != null) {
            val text = StyledText()
            text.append(context.getString(R.string.prefix_created))
            text.append(createdAt)
            holder.root.tv_gist_creation.text = text
            holder.root.tv_gist_creation.visibility = VISIBLE
        } else {
            holder.root.tv_gist_creation.visibility = GONE
        }

        val updatedAt = gist.updatedAt()
        if (updatedAt != null && updatedAt != createdAt) {
            val text = StyledText()
            text.append(context.getString(R.string.prefix_updated))
            text.append(updatedAt)
            holder.root.tv_gist_updated.text = text
            holder.root.tv_gist_updated.visibility = VISIBLE
        } else {
            holder.root.tv_gist_updated.visibility = GONE
        }

        val desc = gist.description()
        if (!desc.isNullOrEmpty()) {
            holder.root.tv_gist_description.text = desc
        } else {
            holder.root.tv_gist_description.setText(R.string.no_description_given)
        }
    }

    override fun getLayout() = R.layout.gist_header

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView) {

        init {
            root.tv_gist_description.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
