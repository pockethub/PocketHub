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

import android.app.Activity.RESULT_OK
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.github.pockethub.android.Intents.*
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.*
import com.github.pockethub.android.core.issue.IssueFilter
import com.github.pockethub.android.core.issue.IssueStore
import com.github.pockethub.android.persistence.AccountDataManager
import com.github.pockethub.android.ui.PagedItemFragment
import com.github.pockethub.android.ui.item.issue.IssueFilterHeaderItem
import com.github.pockethub.android.ui.item.issue.IssueItem
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.service.issues.IssueService
import com.xwray.groupie.Item
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject

/**
 * Fragment to display a list of issues
 */
class IssuesFragment : PagedItemFragment<Issue>() {

    @Inject
    lateinit var cache: AccountDataManager

    @Inject
    lateinit var store: IssueStore

    @Inject
    lateinit var avatars: AvatarLoader

    private var service = ServiceGenerator.createService(activity, IssueService::class.java)
    private var filter: IssueFilter? = null
    private var repository: Repository? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        filter = getParcelableExtra(EXTRA_ISSUE_FILTER)
        repository = getParcelableExtra(EXTRA_REPOSITORY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (filter == null) {
            filter = IssueFilter(repository)
        }
    }

    override fun configureList(recyclerView: RecyclerView) {
        super.configureList(recyclerView)
        mainSection.setHeader(IssueFilterHeaderItem(avatars, filter))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setEmptyText(R.string.no_issues)
    }

    override fun onItemClick(item: Item<*>, view: View) {
        if (item is IssueItem) {
            // Remove one since we have a header
            val position = listAdapter.getAdapterPosition(item) - 1
            val issues = items
                    .filterIsInstance<IssueItem>()
                    .map { it.data }

            startActivityForResult(IssuesViewActivity.createIntent(issues, repository, position), ISSUE_VIEW)
        } else if (item is IssueFilterHeaderItem) {
            startActivityForResult(EditIssuesFilterActivity.createIntent(filter), ISSUE_FILTER_EDIT)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_issues, menu)

        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.m_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

        val args = Bundle()
        args.putParcelable(EXTRA_REPOSITORY, repository)
        searchView.setAppSearchData(args)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!isAdded) {
            return false
        }
        when (item.itemId) {
            R.id.m_refresh -> {
                forceRefresh()
                return true
            }
            R.id.create_issue -> {
                startActivityForResult(EditIssueActivity.createIntent(repository), ISSUE_CREATE)
                return true
            }
            R.id.m_filter -> {
                startActivityForResult(EditIssuesFilterActivity.createIntent(filter), ISSUE_FILTER_EDIT)
                return true
            }
            R.id.m_bookmark -> {
                cache.addIssueFilter(filter)
                        .subscribe { _ -> ToastUtils.show(activity, R.string.message_filter_saved) }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == ISSUE_FILTER_EDIT && data != null) {
            val newFilter = data.getParcelableExtra<IssueFilter>(EXTRA_ISSUE_FILTER)
            if (filter != newFilter) {
                filter = newFilter
                mainSection.setHeader(IssueFilterHeaderItem(avatars, filter))
                refreshWithProgress()
                return
            }
        }

        if (requestCode == ISSUE_VIEW) {
            notifyDataSetChanged()
            forceRefresh()
            return
        }

        if (requestCode == ISSUE_CREATE && resultCode == RESULT_OK) {
            val created = data!!.getParcelableExtra<Issue>(EXTRA_ISSUE)
            forceRefresh()
            startActivityForResult(IssuesViewActivity.createIntent(created, repository), ISSUE_VIEW)
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun loadData(page: Int): Single<Response<Page<Issue>>> {
        return service.getRepositoryIssues(repository!!.owner()!!.login(), repository!!.name(), filter!!.toFilterMap(), page.toLong())
    }

    override fun getLoadingMessage() = R.string.loading_issues

    override fun getErrorMessage() = R.string.error_issues_load

    override fun createItem(item: Issue) = IssueItem(avatars, item)
}
