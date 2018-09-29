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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.pockethub.android.Intents.EXTRA_REPOSITORY
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.PagedListFetcher
import com.github.pockethub.android.PagedScrollListener
import com.github.pockethub.android.R
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.ContributorItem
import com.github.pockethub.android.ui.user.UserViewActivity
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User
import com.meisolsson.githubsdk.service.repositories.RepositoryService
import com.meisolsson.githubsdk.service.users.UserService
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import retrofit2.Response
import javax.inject.Inject

/**
 * Fragment to display a list of contributors for a specific repository
 */
class RepositoryContributorsFragment : BaseFragment() {

    @Inject
    protected lateinit var service: RepositoryService

    /**
     * Avatar loader
     */
    @Inject
    protected lateinit var avatars: AvatarLoader

    private lateinit var pagedListFetcher: PagedListFetcher<User>

    private lateinit var itemListHandler: ItemListHandler

    private lateinit var pagedScrollListener: PagedScrollListener

    private var repo: Repository? = null

    protected val errorMessage: Int
        get() = R.string.error_contributors_load

    protected val loadingMessage: Int
        get() = R.string.loading

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        repo = getParcelableExtra(EXTRA_REPOSITORY)
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
        itemListHandler.setEmptyText(R.string.no_contributors)
    }

    private fun loadData(page: Int): Single<Response<Page<User>>> {
        return service.getContributors(repo!!.owner()!!.login(), repo!!.name(), page.toLong())
    }

    private fun createItem(item: User): Item<*> {
        return ContributorItem(avatars, item)
    }

    fun onItemClick(item: Item<*>, view: View) {
        if (item is ContributorItem) {
            val contributor = item.user
            ServiceGenerator.createService(context, UserService::class.java)
                .getUser(contributor.login())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe { response -> startActivity(UserViewActivity.createIntent(response.body())) }
        }
    }
}
