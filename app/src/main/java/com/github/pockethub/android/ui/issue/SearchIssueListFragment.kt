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
package com.github.pockethub.android.ui.issue

import android.app.SearchManager.APP_DATA
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.pockethub.android.Intents.EXTRA_REPOSITORY
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.PagedListFetcher
import com.github.pockethub.android.PagedScrollListener
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.issue.IssueItem
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.InfoUtils
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.service.search.SearchService
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import retrofit2.Response
import javax.inject.Inject

/**
 * Fragment to display a list of [Issue] instances
 */
class SearchIssueListFragment : BaseFragment() {

    @Inject
    protected lateinit var service: SearchService

    @Inject
    protected lateinit var avatars: AvatarLoader

    lateinit var pagedListFetcher: PagedListFetcher<Issue>

    private lateinit var itemListHandler: ItemListHandler

    private lateinit var pagedScrollListener: PagedScrollListener

    private var repository: Repository? = null

    private var query: String? = null

    protected val errorMessage: Int
        get() = R.string.error_issues_load

    protected val loadingMessage: Int
        get() = R.string.loading_issues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appData = activity!!.intent.getBundleExtra(APP_DATA)
        if (appData != null) {
            repository = appData.getParcelable(EXTRA_REPOSITORY)
        }
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
        itemListHandler.setEmptyText(R.string.no_issues)
    }
    /**
     * @param query
     * @return this fragment
     */
    fun setQuery(query: String): SearchIssueListFragment {
        this.query = query
        return this
    }

    fun onItemClick(item: Item<*>, view: View) {
        if (item is IssueItem) {
            val searchIssue = item.issue
            startActivity(IssuesViewActivity.createIntent(searchIssue, repository))
        }
    }

    private fun loadData(page: Int): Single<Response<Page<Issue>>> {
        val searchQuery = query + "+repo:" + InfoUtils.createRepoId(repository!!)
        return service.searchIssues(searchQuery, null, null, page.toLong())
            .map { response ->
                val issueSearchPage = response.body()

                Response.success(Page.builder<Issue>()
                    .first(issueSearchPage.first())
                    .last(issueSearchPage.last())
                    .next(issueSearchPage.next())
                    .prev(issueSearchPage.prev())
                    .items(issueSearchPage.items())
                    .build())
            }
    }

    private fun createItem(item: Issue): Item<*> {
        return IssueItem(avatars, item, false)
    }
}
