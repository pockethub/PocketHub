package com.github.pockethub.android.ui.item.gist

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.ui.view.LinkTextView
import com.meisolsson.githubsdk.model.Gist

class GistHeaderItem(private val context: Context, dataItem: Gist) : BaseDataItem<Gist, GistHeaderItem.ViewHolder>(null, dataItem, dataItem.id()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.gist_header

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val createdAt = data.createdAt()
        if (createdAt != null) {
            val text = StyledText()
            text.append(context.getString(R.string.prefix_created))
            text.append(createdAt)
            holder.created.text = text
            holder.created.visibility = VISIBLE
        } else {
            holder.created.visibility = GONE
        }

        val updatedAt = data.updatedAt()
        if (updatedAt != null && updatedAt != createdAt) {
            val text = StyledText()
            text.append(context.getString(R.string.prefix_updated))
            text.append(updatedAt)
            holder.updated.text = text
            holder.updated.visibility = VISIBLE
        } else {
            holder.updated.visibility = GONE
        }

        val desc = data.description()
        if (!TextUtils.isEmpty(desc)) {
            holder.description.text = desc
        } else {
            holder.description.setText(R.string.no_description_given)
        }
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_gist_creation)
        lateinit var created: TextView

        @BindView(R.id.tv_gist_description)
        lateinit var description: LinkTextView

        @BindView(R.id.tv_gist_updated)
        lateinit var updated: TextView
    }
}
