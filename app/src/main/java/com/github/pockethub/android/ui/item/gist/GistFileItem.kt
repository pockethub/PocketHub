package com.github.pockethub.android.ui.item.gist

import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.meisolsson.githubsdk.model.GistFile

class GistFileItem(dataItem: GistFile) : BaseDataItem<GistFile, GistFileItem.ViewHolder>(null, dataItem, dataItem.filename()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.gist_file_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holer: ViewHolder, position: Int) {
        holer.filename.text = data.filename()
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_file)
        lateinit var filename: TextView
    }
}
