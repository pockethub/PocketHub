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
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Bundle
import android.view.MenuItem
import com.github.pockethub.android.Intents.Builder
import com.github.pockethub.android.Intents.EXTRA_ISSUE_NUMBERS
import com.github.pockethub.android.Intents.EXTRA_POSITION
import com.github.pockethub.android.Intents.EXTRA_REPOSITORIES
import com.github.pockethub.android.Intents.EXTRA_REPOSITORY
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueStore
import com.github.pockethub.android.core.issue.IssueUtils
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.ui.PagerHandler
import com.github.pockethub.android.ui.repo.RepositoryViewActivity
import com.github.pockethub.android.ui.user.UriLauncherActivity
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.InfoUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User
import com.meisolsson.githubsdk.service.repositories.RepositoryService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_pager.*
import java.util.ArrayList
import javax.inject.Inject

/**
 * Activity to display a collection of issues or pull requests in a pager
 */
class IssuesViewActivity : BaseActivity() {

    @Inject
    lateinit var avatars: AvatarLoader

    @Inject
    lateinit var store: IssueStore

    private var issueNumbers: IntArray? = null

    private var pullRequests: BooleanArray? = null

    private var repoIds: ArrayList<Repository>? = null

    private var repo: Repository? = null

    private var user: User? = null

    private var canWrite: Boolean = false

    private var pagerHandler: PagerHandler<IssuesPagerAdapter>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)

        issueNumbers = getIntArrayExtra(EXTRA_ISSUE_NUMBERS)
        pullRequests = getBooleanArrayExtra(EXTRA_PULL_REQUESTS)
        repoIds = intent.getParcelableArrayListExtra(EXTRA_REPOSITORIES)
        repo = getParcelableExtra(EXTRA_REPOSITORY)

        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        if (repo != null) {
            actionBar.subtitle = InfoUtils.createRepoId(repo!!)
            user = repo!!.owner()
            avatars.bind(actionBar, user!!)
        }

        // Load avatar if single issue and user is currently unset or missing
        // avatar URL
        if (repo == null) {
            val temp = if (repo != null) repo else repoIds!![0]
            ServiceGenerator.createService(this, RepositoryService::class.java)
                .getRepository(temp!!.owner()!!.login(), temp.name())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe { response -> repositoryLoaded(response.body()) }
        } else {
            repositoryLoaded(repo!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(pagerHandler!!)
    }

    private fun repositoryLoaded(repo: Repository) {
        if (issueNumbers!!.size == 1 && (user == null || user!!.avatarUrl() == null)) {
            avatars.bind(supportActionBar!!, repo.owner()!!)
        }

        val permission = repo.permissions()
        canWrite = permission != null && (permission.admin()!! || permission.push()!!)

        invalidateOptionsMenu()
        configurePager()
    }

    private fun configurePager() {
        val initialPosition = getIntExtra(EXTRA_POSITION)

        val adapter = if (repo != null) {
            IssuesPagerAdapter(this, repo, issueNumbers, canWrite)
        } else {
            IssuesPagerAdapter(this, repoIds, issueNumbers, store, canWrite)
        }

        pagerHandler = PagerHandler(this, vp_pages, adapter)
        pagerHandler!!.onPagedChanged = this::onPageChange
        lifecycle.addObserver(pagerHandler!!)
        vp_pages.scheduleSetItem(initialPosition, pagerHandler)
    }

    private fun updateTitle(position: Int) {
        val number = issueNumbers!![position]
        val pullRequest = pullRequests!![position]

        supportActionBar!!.title = if (pullRequest) {
            getString(R.string.pull_request_title) + number
        } else {
            getString(R.string.issue_title) + number
        }
    }

    private fun onPageChange(position: Int) {
        if (repo != null) {
            updateTitle(position)
            return
        }

        if (repoIds == null) {
            return
        }

        val actionBar = supportActionBar!!
        repo = repoIds!![position]
        if (repo != null) {
            updateTitle(position)
            actionBar.subtitle = InfoUtils.createRepoId(repo!!)
            val issue = store.getIssue(repo, issueNumbers!![position])
            if (issue != null) {
                val fullRepo = issue.repository()
                if (fullRepo?.owner() != null) {
                    user = fullRepo.owner()
                    avatars.bind(actionBar, user!!)
                } else {
                    actionBar.setLogo(null)
                }
            } else {
                actionBar.setLogo(null)
            }
        } else {
            actionBar.subtitle = null
            actionBar.setLogo(null)
        }
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, arguments: Bundle) {
        pagerHandler!!.adapter
            .onDialogResult(vp_pages.currentItem, requestCode, resultCode, arguments)
    }

    override fun startActivity(intent: Intent) {
        val converted = UriLauncherActivity.convert(intent)
        if (converted != null) {
            super.startActivity(converted)
        } else {
            super.startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                var repository = repo
                if (repository == null) {
                    val position = vp_pages.currentItem
                    val repoId = repoIds!![position]
                    val issue = store.getIssue(repoId,
                        issueNumbers!![position])
                    if (issue != null) {
                        repository = issue.repository()
                    }
                }
                if (repository != null) {
                    val intent = RepositoryViewActivity.createIntent(repository)
                    intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private val EXTRA_PULL_REQUESTS = "pullRequests"

        /**
         * Create an intent to show a single issue
         *
         * @param issue
         * @return intent
         */
        fun createIntent(issue: Issue): Intent {
            return createIntent(listOf(issue), 0)
        }

        /**
         * Create an intent to show issue
         *
         * @param issue
         * @param repository
         * @return intent
         */
        fun createIntent(issue: Issue,
            repository: Repository): Intent {
            return createIntent(listOf(issue), repository, 0)
        }

        /**
         * Create an intent to show issues with an initial selected issue
         *
         * @param issues
         * @param repository
         * @param position
         * @return intent
         */
        fun createIntent(issues: Collection<Issue>,
            repository: Repository, position: Int): Intent {
            val numbers = IntArray(issues.size)
            val pullRequests = BooleanArray(issues.size)
            for ((index, issue) in issues.withIndex()) {
                numbers[index] = issue.number()!!
                pullRequests[index] = IssueUtils.isPullRequest(issue)
            }
            return Builder("issues.VIEW").add(EXTRA_ISSUE_NUMBERS, numbers)
                .add(EXTRA_REPOSITORY, repository)
                .add(EXTRA_POSITION, position)
                .add(EXTRA_PULL_REQUESTS, pullRequests).toIntent()
        }

        /**
         * Create an intent to show issues with an initial selected issue
         *
         * @param issues
         * @param position
         * @return intent
         */
        fun createIntent(issues: Collection<Issue>,
            position: Int): Intent {
            val count = issues.size
            val numbers = IntArray(count)
            val pullRequests = BooleanArray(count)
            val repos = ArrayList<Repository>(count)
            for ((index, issue) in issues.withIndex()) {
                numbers[index] = issue.number()!!
                pullRequests[index] = IssueUtils.isPullRequest(issue)

                var repoId: Repository? = null
                val issueRepo = issue.repository()
                if (issueRepo != null) {
                    val owner = issueRepo.owner()
                    if (owner != null) {
                        repoId = InfoUtils.createRepoFromData(owner.login(), issueRepo.name())
                    }
                }
                if (repoId == null) {
                    repoId = InfoUtils.createRepoFromUrl(issue.htmlUrl())
                }
                repos.add(repoId!!)
            }

            val builder = Builder("issues.VIEW")
            builder.add(EXTRA_ISSUE_NUMBERS, numbers)
            builder.add(EXTRA_REPOSITORIES, repos)
            builder.add(EXTRA_POSITION, position)
            builder.add(EXTRA_PULL_REQUESTS, pullRequests)
            return builder.toIntent()
        }
    }
}
