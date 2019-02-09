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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.PagedListFetcher
import com.github.pockethub.android.PagedScrollListener
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.ISSUE_VIEW
import com.github.pockethub.android.core.issue.IssueStore
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.issue.IssueDashboardItem
import com.github.pockethub.android.ui.item.issue.IssueItem
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.service.issues.IssueService
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import retrofit2.Response
import java.util.ArrayList
import javax.inject.Inject

/**
 * Fragment to display a pageable list of dashboard issues
 */
class DashboardIssueFragment : BaseFragment() {

    @Inject
    protected lateinit var service: IssueService

    @Inject
    protected lateinit var store: IssueStore

    @Inject
    protected lateinit var avatars: AvatarLoader

    private lateinit var pagedListFetcher: PagedListFetcher<Issue>

    private lateinit var itemListHandler: ItemListHandler

    private lateinit var pagedScrollListener: PagedScrollListener

    private var filterData: Map<String, Any>? = null

    protected val loadingMessage: Int
        get() = R.string.loading_issues

    protected val errorMessage: Int
        get() = R.string.error_issues_load

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        filterData = arguments!!.getSerializable(ARG_FILTER) as Map<String, Any>
        super.onActivityCreated(savedInstanceState)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ISSUE_VIEW) {
            pagedListFetcher.refresh()
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadData(page: Int): Single<Response<Page<Issue>>> {
        return service.getIssues(filterData, page.toLong())
    }

    private fun onItemClick(clickedItem: Item<*>, view: View) {
        if (clickedItem is IssueDashboardItem) {
            val position = itemListHandler.getItemPosition(clickedItem)
            val issues = ArrayList<Issue>()
            for (item in itemListHandler.items) {
                if (item is IssueDashboardItem) {
                    issues.add((item as IssueItem).issue)
                }
            }
            startActivityForResult(IssuesViewActivity.createIntent(issues, position), ISSUE_VIEW)
        }
    }

    private fun createItem(item: Issue): Item<*> {
        return IssueDashboardItem(avatars, item)
    }

    companion object {

        /**
         * Filter data argument
         */
        val ARG_FILTER = "filter"
    }
}
