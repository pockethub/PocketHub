/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.android.ui.gist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.PagedListFetcher
import com.github.pockethub.android.PagedScrollListener
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.GIST_CREATE
import com.github.pockethub.android.RequestCodes.GIST_VIEW
import com.github.pockethub.android.core.gist.GistStore
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.gist.GistItem
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.Gist
import com.meisolsson.githubsdk.model.Page
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import retrofit2.Response
import javax.inject.Inject

/**
 * Fragment to display a list of Gists
 */
abstract class GistsFragment : BaseFragment() {

    /**
     * Avatar loader
     */
    @Inject
    protected lateinit var avatars: AvatarLoader

    /**
     * Gist store
     */
    @Inject
    protected lateinit var store: GistStore

    protected lateinit var pagedListFetcher: PagedListFetcher<Gist>

    private lateinit var itemListHandler: ItemListHandler

    private lateinit var pagedScrollListener: PagedScrollListener

    protected val errorMessage: Int
        get() = R.string.error_gists_load

    protected val loadingMessage: Int
        get() = R.string.loading_gists

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemListHandler = ItemListHandler(
            view.list,
            view.empty,
            lifecycle,
            activity,
            OnItemClickListener(this::onItemClick)
        )

        pagedListFetcher = PagedListFetcher(
            view.swipe_item,
            lifecycle,
            itemListHandler,
            { t -> ToastUtils.show(activity, errorMessage)},
            this::loadData,
            this::createItem
        )

        pagedScrollListener = PagedScrollListener(
            itemListHandler.mainSection,
            pagedListFetcher,
            view.list,
            loadingMessage
        )
        itemListHandler.setEmptyText(R.string.no_gists)
    }

    protected abstract fun loadData(page: Int): Single<Response<Page<Gist>>>

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!isAdded) {
            return false
        }
        return when (item.itemId) {
            R.id.m_create -> {
                startActivityForResult(Intent(activity,
                    CreateGistActivity::class.java), GIST_CREATE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GIST_VIEW || requestCode == GIST_CREATE) {
            pagedListFetcher.refresh()
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    protected fun createItem(item: Gist): Item<*> {
        return GistItem(avatars, item)
    }

    fun onItemClick(item: Item<*>, view: View) {
        val position = itemListHandler.getItemPosition(item)
        val intent = GistsViewActivity.createIntent(itemListHandler.items, position)
        startActivityForResult(intent, GIST_VIEW)
    }
}
