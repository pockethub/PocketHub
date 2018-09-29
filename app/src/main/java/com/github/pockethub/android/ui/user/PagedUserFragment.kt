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
package com.github.pockethub.android.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.PagedListFetcher
import com.github.pockethub.android.PagedScrollListener
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.UserItem
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.User
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import retrofit2.Response
import javax.inject.Inject

/**
 * Fragment to page over users
 */
abstract class PagedUserFragment : BaseFragment() {

    /**
     * Avatar loader
     */
    @Inject
    protected lateinit var avatars: AvatarLoader

    private lateinit var pagedListFetcher: PagedListFetcher<User>

    private lateinit var itemListHandler: ItemListHandler

    private lateinit var pagedScrollListener: PagedScrollListener

    protected abstract val loadingMessage: Int

    protected abstract val errorMessage: Int

    protected abstract val emptyText: Int

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
        itemListHandler.setEmptyText(emptyText)
    }

    protected abstract fun loadData(page: Int): Single<Response<Page<User>>>

    protected fun createItem(item: User): Item<*> {
        return UserItem(avatars, item)
    }

    fun onItemClick(item: Item<*>, view: View) {
        if (item is UserItem) {
            val user = item.user
            startActivity(UserViewActivity.createIntent(user))
        }
    }
}
