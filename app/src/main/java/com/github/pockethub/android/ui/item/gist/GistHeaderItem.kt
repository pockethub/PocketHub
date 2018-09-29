package com.github.pockethub.android.ui.item.gist

import android.content.Context
import android.text.TextUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.R
import com.meisolsson.githubsdk.model.Gist
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.gist_header.*

class GistHeaderItem(
        private val context: Context,
        val gist: Gist
) : Item(gist.id()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.gist_header

    override fun bind(holder: ViewHolder, position: Int) {
        val createdAt = gist.createdAt()
        if (createdAt != null) {
            holder.tv_gist_creation.text = buildSpannedString {
                append("${context.getString(R.string.prefix_created)}$createdAt")
            }
            holder.tv_gist_creation.visibility = VISIBLE
        } else {
            holder.tv_gist_creation.visibility = GONE
        }

        val updatedAt = gist.updatedAt()
        if (updatedAt != null && updatedAt != createdAt) {
            holder.tv_gist_updated.text = buildSpannedString {
                append("$context.getString(R.string.prefix_updated)}$updatedAt")
            }
            holder.tv_gist_updated.visibility = VISIBLE
        } else {
            holder.tv_gist_updated.visibility = GONE
        }

        val desc = gist.description()
        if (!TextUtils.isEmpty(desc)) {
            holder.tv_gist_description.text = desc
        } else {
            holder.tv_gist_description.setText(R.string.no_description_given)
        }
    }
}
