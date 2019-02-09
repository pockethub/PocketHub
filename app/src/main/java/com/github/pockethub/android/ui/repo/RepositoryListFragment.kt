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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.github.pockethub.android.Intents.EXTRA_USER
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.ListFetcher
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.REPOSITORY_VIEW
import com.github.pockethub.android.ResultCodes.RESOURCE_CHANGED
import com.github.pockethub.android.persistence.AccountDataManager
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.repository.RepositoryHeaderItem
import com.github.pockethub.android.ui.item.repository.RepositoryItem
import com.github.pockethub.android.ui.user.OrganizationSelectionListener
import com.github.pockethub.android.ui.user.OrganizationSelectionProvider
import com.github.pockethub.android.ui.user.UserViewActivity
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.InfoUtils
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.OnItemLongClickListener
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import kotlinx.android.synthetic.main.repo_dialog.view.*
import java.util.Locale.US
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * Fragment to display a list of [Repository] instances
 */
class RepositoryListFragment : BaseFragment(), OrganizationSelectionListener {

    @Inject
    protected lateinit var cache: AccountDataManager

    @Inject
    protected lateinit var avatars: AvatarLoader

    private lateinit var itemListHandler: ItemListHandler

    private lateinit var listFetcher: ListFetcher<Repository>

    private val org = AtomicReference<User>()

    private var recentRepos: RecentRepositories? = null

    private val errorMessage: Int
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
            OnItemClickListener(this::onItemClick),
            OnItemLongClickListener(this::onItemLongClick)
        )

        listFetcher = ListFetcher(
            view.swipe_item,
            lifecycle,
            itemListHandler,
            { t -> ToastUtils.show(activity, errorMessage)},
            this::loadData,
            this::createItem
        )

        listFetcher.onDataLoaded = this::onDataLoaded
        itemListHandler.setEmptyText(R.string.no_repositories)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val org = this.org.get()
        if (org != null) {
            outState.putParcelable(EXTRA_USER, org)
        }
    }

    override fun onDetach() {
        if (activity != null && activity is OrganizationSelectionProvider) {
            val selectionProvider = activity as OrganizationSelectionProvider?
            selectionProvider!!.removeListener(this)
        }

        super.onDetach()
    }

    override fun onOrganizationSelected(organization: User) {
        val previousOrg = org.get()
        val previousOrgId = if (previousOrg != null) previousOrg.id() else -1
        org.set(organization)

        if (recentRepos != null) {
            recentRepos!!.saveAsync()
        }

        // Only hard refresh if view already created and org is changing
        if (previousOrgId != organization.id()) {
            val activity = activity
            if (activity != null) {
                recentRepos = RecentRepositories(activity, organization)
            }

            listFetcher.forceRefresh()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val activity = activity
        var currentOrg: User? = null

        if (getActivity() is OrganizationSelectionProvider) {
            currentOrg = (activity as OrganizationSelectionProvider).addListener(this)
        }

        if (arguments != null && arguments!!.containsKey("org")) {
            currentOrg = arguments!!.getParcelable("org")
        }

        if (currentOrg == null && savedInstanceState != null) {
            currentOrg = savedInstanceState.getParcelable(EXTRA_USER)
        }

        org.set(currentOrg)
        if (currentOrg != null) {
            recentRepos = RecentRepositories(activity, currentOrg)
        }

        super.onActivityCreated(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Refresh if the viewed repository was (un)starred
        if (requestCode == REPOSITORY_VIEW && resultCode == RESOURCE_CHANGED) {
            listFetcher.forceRefresh()
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun onItemClick(item: Item<*>, view: View) {
        if (item is RepositoryItem) {
            val repo = item.repo
            if (recentRepos != null) {
                recentRepos!!.add(repo)
            }

            startActivityForResult(RepositoryViewActivity.createIntent(repo),
                REPOSITORY_VIEW)
        }
    }

    private fun onItemLongClick(item: Item<*>, view: View): Boolean {
        if (!isAdded) {
            return false
        }

        if (item is RepositoryItem) {
            val repo = item.repo

            val builder = MaterialDialog.Builder(activity!!).title(InfoUtils.createRepoId(repo))
            val dialogHolder = arrayOfNulls<MaterialDialog>(1)

            val dialogView = layoutInflater.inflate(R.layout.repo_dialog, null)

            val owner = repo.owner()
            avatars.bind(dialogView.iv_owner_avatar, owner)
            dialogView.tv_owner_name.text = getString(R.string.navigate_to_user, owner!!.login())
            dialogView.ll_owner_area.setOnClickListener { v1 ->
                dialogHolder[0]!!.dismiss()
                viewUser(owner)
            }

            if (recentRepos != null && recentRepos!!.contains(repo)) {
                dialogView.divider.visibility = View.VISIBLE
                val recentRepoArea = dialogView.ll_recent_repo_area
                recentRepoArea.visibility = View.VISIBLE
                recentRepoArea.setOnClickListener { v1 ->
                    dialogHolder[0]!!.dismiss()
                    recentRepos!!.remove(repo)
                    listFetcher.refresh()
                }
            }

            builder.customView(dialogView, false)
            val dialog = builder.build()
            dialogHolder[0] = dialog
            dialog.setCanceledOnTouchOutside(true)
            dialog.show()

            return true
        }

        return false
    }

    private fun viewUser(user: User) {
        if (org.get().id() !== user.id()) {
            startActivity(UserViewActivity.createIntent(user))
        }
    }

    override fun onStop() {
        super.onStop()

        if (recentRepos != null) {
            recentRepos!!.saveAsync()
        }
    }

    private fun updateHeaders(repos: MutableList<Item<*>>) {
        if (repos.isEmpty()) {
            return
        }

        // Add recent header if at least one recent repository
        val first = (repos[0] as RepositoryItem).repo
        if (recentRepos!!.contains(first)) {
            repos.add(0, RepositoryHeaderItem(getString(R.string.recently_viewed)))
        }

        // Advance past all recent repositories
        var index = 0
        while (index < repos.size) {
            val item = repos[index]
            if (item is RepositoryItem) {
                val repository = item.repo
                if (!recentRepos!!.contains(repository.id()!!)) {
                    break
                }
            }
            index++
        }

        if (index >= repos.size) {
            return
        }

        // Register header for first character
        var current = (repos[index] as RepositoryItem).repo
        var start = Character.toLowerCase(current.name()!![0])
        repos.add(index, RepositoryHeaderItem(Character.toString(start).toUpperCase(US)))

        index += 1
        while (index < repos.size) {
            current = (repos[index] as RepositoryItem).repo
            val repoStart = Character.toLowerCase(current.name()!![0])
            if (repoStart <= start) {
                index++
                continue
            }

            repos.add(index, RepositoryHeaderItem(Character.toString(repoStart).toUpperCase(US)))
            start = repoStart
            index++
        }
    }

    private fun loadData(forceRefresh: Boolean): Single<List<Repository>> {
        val org = this.org.get() ?: return Single.just(emptyList())

        return Single.fromCallable { cache.getRepos(org, forceRefresh) }
            .flatMap { repos ->
                Observable.fromIterable(repos)
                    .sorted(recentRepos!!)
                    .toList()
            }
    }

    private fun createItem(item: Repository): Item<*> {
        return RepositoryItem(item, org.get())
    }

    protected fun onDataLoaded(newItems: MutableList<Item<*>>): MutableList<Item<*>> {
        updateHeaders(newItems)
        return newItems
    }
}
