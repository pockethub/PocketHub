package com.github.pockethub.android.ui.helpers

import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.Section

class PagedScrollListener(
    private val section: Section,
    private val listFetcher: PagedListFetcher<*>,
    recyclerView: RecyclerView,
    @StringRes loadingMessage: Int
) : RecyclerView.OnScrollListener() {

    private val loadingIndicator: ResourceLoadingIndicator =
        ResourceLoadingIndicator(loadingMessage, section)

    init {
        recyclerView.addOnScrollListener(this)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (!listFetcher.hasMore) {
            loadingIndicator.visible = false
            return
        }

        // The item count minus the footer
        val count = section.itemCount - 1
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        if (layoutManager.findLastVisibleItemPosition() >= count) {
            listFetcher.fetchNext()
            loadingIndicator.visible = true
        }
    }
}