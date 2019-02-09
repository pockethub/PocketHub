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
package com.github.pockethub.android.ui

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.CATEGORY_BROWSABLE
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.PagedListFetcher
import com.github.pockethub.android.PagedScrollListener
import com.github.pockethub.android.R
import com.github.pockethub.android.core.gist.GistEventMatcher
import com.github.pockethub.android.core.issue.IssueEventMatcher
import com.github.pockethub.android.core.repo.RepositoryEventMatcher
import com.github.pockethub.android.core.user.UserEventMatcher
import com.github.pockethub.android.core.user.UserEventMatcher.UserPair
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.commit.CommitCompareViewActivity
import com.github.pockethub.android.ui.commit.CommitViewActivity
import com.github.pockethub.android.ui.gist.GistsViewActivity
import com.github.pockethub.android.ui.issue.IssuesViewActivity
import com.github.pockethub.android.ui.item.news.NewsItem
import com.github.pockethub.android.ui.repo.RepositoryViewActivity
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ConvertUtils
import com.github.pockethub.android.util.InfoUtils
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.GitHubEventType.CommitCommentEvent
import com.meisolsson.githubsdk.model.GitHubEventType.DownloadEvent
import com.meisolsson.githubsdk.model.GitHubEventType.PushEvent
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User
import com.meisolsson.githubsdk.model.payload.CommitCommentPayload
import com.meisolsson.githubsdk.model.payload.PushPayload
import com.meisolsson.githubsdk.model.payload.ReleasePayload
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.OnItemLongClickListener
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import kotlinx.android.synthetic.main.nav_dialog.view.*
import retrofit2.Response
import javax.inject.Inject

/**
 * Base news fragment class with utilities for subclasses to built on
 */
abstract class NewsFragment : BaseFragment() {

    @Inject
    protected lateinit var avatars: AvatarLoader

    protected lateinit var pagedListFetcher: PagedListFetcher<GitHubEvent>

    private lateinit var itemListHandler: ItemListHandler

    private lateinit var pagedScrollListener: PagedScrollListener

    protected val loadingMessage: Int
        get() = R.string.loading_news

    protected val errorMessage: Int
        get() = R.string.error_news_load

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
        itemListHandler.setEmptyText(R.string.no_news)
    }

    protected abstract fun loadData(page: Int): Single<Response<Page<GitHubEvent>>>

    fun onItemClick(item: Item<*>, view: View) {
        if (item !is NewsItem) {
            return
        }

        val event = item.gitHubEvent

        if (DownloadEvent == event.type()) {
            openDownload(event)
            return
        }

        if (PushEvent == event.type()) {
            openPush(event)
            return
        }

        if (CommitCommentEvent == event.type()) {
            openCommitComment(event)
            return
        }

        val issue = IssueEventMatcher.getIssue(event)
        if (issue != null) {
            val repo = ConvertUtils.eventRepoToRepo(event.repo()!!)
            viewIssue(issue, repo)
            return
        }

        val gist = GistEventMatcher.getGist(event)
        if (gist != null) {
            startActivity(GistsViewActivity.createIntent(gist))
            return
        }

        val repo = RepositoryEventMatcher.getRepository(event)
        if (repo != null) {
            viewRepository(repo)
        }

        val users = UserEventMatcher.getUsers(event)
        if (users != null) {
            viewUser(users)
        }
    }

    private fun onItemLongClick(item: Item<*>, view: View): Boolean {
        if (!isAdded) {
            return false
        }

        if (item !is NewsItem) {
            return false
        }

        val event = item.gitHubEvent
        val repo = ConvertUtils.eventRepoToRepo(event.repo()!!)
        val user = event.actor()

        if (repo != null && user != null) {
            val builder = MaterialDialog.Builder(activity!!)
                .title(R.string.navigate_to)

            // Hacky but necessary since material dialogs has a different API
            val dialogHolder = arrayOfNulls<MaterialDialog>(1)

            val dialogView = layoutInflater.inflate(R.layout.nav_dialog, null)
            avatars.bind(dialogView.iv_user_avatar, user)
            avatars.bind(dialogView.iv_repo_avatar, repo.owner())
            dialogView.tv_login.text = user.login()
            dialogView.tv_repo_name.text = InfoUtils.createRepoId(repo)
            dialogView.ll_user_area.setOnClickListener { v1 ->
                dialogHolder[0]!!.dismiss()
                viewUser(user)
            }
            dialogView.ll_repo_area.setOnClickListener { v1 ->
                dialogHolder[0]!!.dismiss()
                viewRepository(repo)
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

    // https://developer.github.com/v3/repos/downloads/#downloads-api-is-deprecated
    private fun openDownload(event: GitHubEvent) {
        val release = (event.payload() as ReleasePayload).release() ?: return

        val url = release.htmlUrl()
        if (TextUtils.isEmpty(url)) {
            return
        }

        val intent = Intent(ACTION_VIEW, Uri.parse(url))
        intent.addCategory(CATEGORY_BROWSABLE)
        startActivity(intent)
    }

    private fun openCommitComment(event: GitHubEvent) {
        var repo: Repository? = ConvertUtils.eventRepoToRepo(event.repo()!!) ?: return

        if (repo!!.name()!!.contains("/")) {
            val repoId = repo.name()!!
                .split("/".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            repo = InfoUtils.createRepoFromData(repoId[0], repoId[1])
        }



        val payload = event.payload() as CommitCommentPayload? ?: return
        val comment = payload.comment() ?: return

        val sha = comment.commitId()
        if (!TextUtils.isEmpty(sha)) {
            startActivity(CommitViewActivity.createIntent(repo!!, sha!!))
        }
    }

    private fun openPush(event: GitHubEvent) {
        val repo = ConvertUtils.eventRepoToRepo(event.repo()!!) ?: return

        val payload = event.payload() as PushPayload? ?: return
        val commits = payload.commits()
        if (commits.isEmpty()) {
            return
        }

        if (commits.size > 1) {
            val base = payload.before()
            val head = payload.head()
            if (!TextUtils.isEmpty(base) && !TextUtils.isEmpty(head)) {
                startActivity(CommitCompareViewActivity.createIntent(repo, base, head))
            }
        } else {
            val commit = commits[0]
            val sha = commit?.sha()
            if (!TextUtils.isEmpty(sha)) {
                startActivity(CommitViewActivity.createIntent(repo, sha!!))
            }
        }
    }

    /**
     * Start an activity to view the given repository
     *
     * @param repository
     */
    protected open fun viewRepository(repository: Repository?) {
        startActivity(RepositoryViewActivity.createIntent(repository!!))
    }

    /**
     * Start an activity to view the given [UserPair]
     *
     *
     * This method does nothing by default, subclasses should override
     *
     * @param users
     */
    protected open fun viewUser(users: UserPair) {}

    /**
     * Start an activity to view the given [User]
     *
     * @param user
     * @return true if new activity started, false otherwise
     */
    protected open fun viewUser(user: User?): Boolean {
        return false
    }

    /**
     * Start an activity to view the given [Issue]
     *
     * @param issue
     * @param repository
     */
    protected open fun viewIssue(issue: Issue, repository: Repository?) {
        if (repository != null) {
            startActivity(IssuesViewActivity.createIntent(issue, repository))
        } else {
            startActivity(IssuesViewActivity.createIntent(issue))
        }
    }

    protected fun createItem(item: GitHubEvent): Item<*> {
        return NewsItem.createNewsItem(avatars, item)!!
    }
}
