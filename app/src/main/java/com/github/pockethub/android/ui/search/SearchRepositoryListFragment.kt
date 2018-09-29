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
package com.github.pockethub.android.ui.search

import android.app.SearchManager.QUERY
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.PagedListFetcher
import com.github.pockethub.android.PagedScrollListener
import com.github.pockethub.android.R
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.rx.RxProgress
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.repository.RepositoryItem
import com.github.pockethub.android.ui.repo.RepositoryViewActivity
import com.github.pockethub.android.util.InfoUtils
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.service.repositories.RepositoryService
import com.meisolsson.githubsdk.service.search.SearchService
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import retrofit2.Response
import java.text.MessageFormat
import javax.inject.Inject

/**
 * Fragment to display a list of [Repository] instances
 */
//Repository
class SearchRepositoryListFragment : BaseFragment() {

    @Inject
    protected lateinit var service: SearchService

    lateinit var pagedListFetcher: PagedListFetcher<Repository>

    private lateinit var itemListHandler: ItemListHandler

    private lateinit var pagedScrollListener: PagedScrollListener

    protected val loadingMessage: Int
        get() = R.string.loading_repositories

    protected val errorMessage: Int
        get() = R.string.error_repos_load

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

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        start()
    }

    private fun start() {
        openRepositoryMatch(getStringExtra(QUERY))
    }

    fun onItemClick(item: Item<*>, view: View) {
        if (item is RepositoryItem) {
            val result = item.repo
            ServiceGenerator.createService(context, RepositoryService::class.java)
                .getRepository(result.owner()!!.login(), result.name())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxProgress.bindToLifecycle(activity,
                    MessageFormat.format(getString(R.string.opening_repository),
                        InfoUtils.createRepoId(result))))
                .`as`(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe { response -> startActivity(RepositoryViewActivity.createIntent(response.body())) }
        }
    }

    /**
     * Check if the search query is an exact repository name/owner match and
     * open the repository activity and finish the current activity when it is
     *
     * @param query
     * @return true if query opened as repository, false otherwise
     */
    private fun openRepositoryMatch(query: String?): Boolean {
        if (TextUtils.isEmpty(query)) {
            return false
        }

        val repoId = InfoUtils.createRepoFromUrl(query!!.trim { it <= ' ' }) ?: return false

        ServiceGenerator.createService(context, RepositoryService::class.java)
            .getRepository(repoId.owner()!!.login(), repoId.name())
            .subscribe { response ->
                if (response.isSuccessful) {
                    startActivity(RepositoryViewActivity.createIntent(response.body()))
                    val activity = activity
                    activity?.finish()
                }
            }

        return true
    }


    private fun loadData(page: Int): Single<Response<Page<Repository>>> {
        return service.searchRepositories(getStringExtra(QUERY), null, null, page.toLong())
            .map { response ->
                val repositorySearchPage = response.body()
                Response.success(Page.builder<Repository>()
                    .first(repositorySearchPage.first())
                    .last(repositorySearchPage.last())
                    .next(repositorySearchPage.next())
                    .prev(repositorySearchPage.prev())
                    .items(repositorySearchPage.items())
                    .build())
            }
    }

    private fun createItem(item: Repository): Item<*> {
        return RepositoryItem(item, null)
    }
}
