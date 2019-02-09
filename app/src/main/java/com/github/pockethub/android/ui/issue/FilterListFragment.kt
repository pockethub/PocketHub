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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.ListFetcher
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueFilter
import com.github.pockethub.android.persistence.AccountDataManager
import com.github.pockethub.android.ui.ConfirmDialogFragment
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.issue.IssueFilterItem
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.InfoUtils
import com.github.pockethub.android.util.ToastUtils
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.OnItemLongClickListener
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import java.lang.String.CASE_INSENSITIVE_ORDER
import java.util.ArrayList
import java.util.Comparator
import javax.inject.Inject

/**
 * Fragment to display a list of [IssueFilter] objects
 */
class FilterListFragment : BaseFragment(), Comparator<IssueFilter> {

    @Inject
    protected lateinit var cache: AccountDataManager

    @Inject
    protected lateinit var avatars: AvatarLoader

    lateinit var listFetcher: ListFetcher<IssueFilter>

    private lateinit var itemListHandler: ItemListHandler

    private val errorMessage: Int
        get() = R.string.error_bookmarks_load

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemListHandler = ItemListHandler(
            view.list,
            view.empty,
            lifecycle,
            activity,
            OnItemClickListener(this::onItemClick),
            OnItemLongClickListener(this::onItemLongClick)
        )
        itemListHandler.setEmptyText(R.string.no_bookmarks)

        listFetcher = ListFetcher(
            view.swipe_item,
            lifecycle,
            itemListHandler,
            { t -> ToastUtils.show(activity, errorMessage)},
            this::loadData,
            this::createItem
        )
    }

    private fun loadData(forceRefresh: Boolean): Single<List<IssueFilter>> {
        return Single.fromCallable { ArrayList(cache.issueFilters) }
            .flatMap { filters ->
                Observable.fromIterable(filters)
                    .sorted(this@FilterListFragment)
                    .toList()
            }
    }

    private fun createItem(item: IssueFilter): Item<*> = IssueFilterItem(avatars, item)

    fun onItemClick(item: Item<*>, view: View) {
        if (item is IssueFilterItem) {
            val filter = item.issueFilter
            startActivity(IssueBrowseActivity.createIntent(filter))
        }
    }

    fun onItemLongClick(@NonNull item: Item<*>, @NonNull view: View): Boolean {
        if (item is IssueFilterItem) {
            val filter = item.issueFilter
            val args = Bundle()
            args.putParcelable(ARG_FILTER, filter)
            ConfirmDialogFragment.show(activity, REQUEST_DELETE,
                getString(R.string.confirm_bookmark_delete_title),
                getString(R.string.confirm_bookmark_delete_message), args)
            return true
        }

        return false
    }

    override fun onResume() {
        super.onResume()
        listFetcher.refresh()
    }

    override fun compare(lhs: IssueFilter, rhs: IssueFilter): Int {
        var compare = CASE_INSENSITIVE_ORDER.compare(InfoUtils.createRepoId(lhs.repository), InfoUtils.createRepoId(rhs.repository))
        if (compare == 0) {
            compare = CASE_INSENSITIVE_ORDER.compare(
                lhs.toDisplay().toString(), rhs.toDisplay().toString())
        }
        return compare
    }

    companion object {

        const val ARG_FILTER = "filter"

        const val REQUEST_DELETE = 1
    }
}
