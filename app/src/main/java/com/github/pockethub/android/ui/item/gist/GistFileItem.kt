package com.github.pockethub.android.ui.item.gist

import com.github.pockethub.android.R
import com.meisolsson.githubsdk.model.GistFile
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.gist_file_item.*

class GistFileItem(val gistFile: GistFile) : Item(gistFile.filename()!!.hashCode().toLong()) {

    override fun getLayout() = R.layout.gist_file_item

    override fun bind(holer: ViewHolder, position: Int) {
        holer.tv_file.text = gistFile.filename()
    }
}
