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
package com.github.pockethub.android.ui.repo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.pockethub.android.Intents.EXTRA_USER
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.PagedListFetcher
import com.github.pockethub.android.PagedScrollListener
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.REPOSITORY_VIEW
import com.github.pockethub.android.ResultCodes.RESOURCE_CHANGED
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.repository.RepositoryItem
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User
import com.meisolsson.githubsdk.service.repositories.RepositoryService
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import retrofit2.Response
import javax.inject.Inject

/**
 * Fragment to display a list of repositories for a [User]
 */
class UserRepositoryListFragment : BaseFragment() {

    @Inject
    protected lateinit var service: RepositoryService

    private lateinit var pagedListFetcher: PagedListFetcher<Repository>

    private lateinit var itemListHandler: ItemListHandler

    private lateinit var pagedScrollListener: PagedScrollListener

    private var user: User? = null

    protected val loadingMessage: Int
        get() = R.string.loading_repositories

    protected val errorMessage: Int
        get() = R.string.error_repos_load

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        user = getParcelableExtra(EXTRA_USER)
    }

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
        itemListHandler.setEmptyText(R.string.no_repositories)
    }

    private fun loadData(page: Int): Single<Response<Page<Repository>>> {
        return service.getUserRepositories(user!!.login(), page.toLong())
    }

    private fun createItem(item: Repository): Item<*> {
        return RepositoryItem(item, user)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REPOSITORY_VIEW && resultCode == RESOURCE_CHANGED) {
            pagedListFetcher.refresh()
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun onItemClick(item: Item<*>, view: View) {
        if (item is RepositoryItem) {
            val repo = item.repo
            startActivityForResult(RepositoryViewActivity.createIntent(repo), REPOSITORY_VIEW)
        }
    }
}
