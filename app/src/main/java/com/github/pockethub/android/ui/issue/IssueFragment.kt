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
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.pockethub.android.Intents.EXTRA_CAN_WRITE_REPO
import com.github.pockethub.android.Intents.EXTRA_COMMENT
import com.github.pockethub.android.Intents.EXTRA_ISSUE
import com.github.pockethub.android.Intents.EXTRA_ISSUE_NUMBER
import com.github.pockethub.android.Intents.EXTRA_REPOSITORY_NAME
import com.github.pockethub.android.Intents.EXTRA_REPOSITORY_OWNER
import com.github.pockethub.android.Intents.EXTRA_USER
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.COMMENT_CREATE
import com.github.pockethub.android.RequestCodes.COMMENT_DELETE
import com.github.pockethub.android.RequestCodes.COMMENT_EDIT
import com.github.pockethub.android.RequestCodes.ISSUE_ASSIGNEE_UPDATE
import com.github.pockethub.android.RequestCodes.ISSUE_CLOSE
import com.github.pockethub.android.RequestCodes.ISSUE_EDIT
import com.github.pockethub.android.RequestCodes.ISSUE_LABELS_UPDATE
import com.github.pockethub.android.RequestCodes.ISSUE_MILESTONE_UPDATE
import com.github.pockethub.android.RequestCodes.ISSUE_REOPEN
import com.github.pockethub.android.accounts.AccountUtils
import com.github.pockethub.android.core.issue.IssueStore
import com.github.pockethub.android.core.issue.IssueUtils
import com.github.pockethub.android.core.issue.RefreshIssueTaskFactory
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.rx.RxProgress
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.ui.ConfirmDialogFragment
import com.github.pockethub.android.ui.DialogResultListener
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.comment.DeleteCommentListener
import com.github.pockethub.android.ui.comment.EditCommentListener
import com.github.pockethub.android.ui.commit.CommitCompareViewActivity
import com.github.pockethub.android.ui.item.GitHubCommentItem
import com.github.pockethub.android.ui.item.LoadingItem
import com.github.pockethub.android.ui.item.issue.IssueEventItem
import com.github.pockethub.android.ui.item.issue.IssueHeaderItem
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.github.pockethub.android.util.InfoUtils
import com.github.pockethub.android.util.ShareUtils
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.GitHubComment
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.IssueEvent
import com.meisolsson.githubsdk.model.IssueState
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User
import com.meisolsson.githubsdk.service.issues.IssueCommentService
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_comment_list.*
import java.util.ArrayList
import javax.inject.Inject

/**
 * Fragment to display an issue
 */
class IssueFragment : BaseFragment(), IssueHeaderItem.OnIssueHeaderActionListener, DialogResultListener {

    private var issueNumber: Int = 0

    private val items: MutableList<Any> = ArrayList()

    private var repositoryId: Repository? = null

    private var issue: Issue? = null

    private var user: User? = null

    private var canWrite: Boolean = false

    private val adapter = GroupAdapter<ViewHolder>()

    private val mainSection = Section()

    private var milestoneTask: EditMilestoneTask? = null

    private var assigneeTask: EditAssigneeTask? = null

    private var labelsTask: EditLabelsTask? = null

    private var stateTask: EditStateTask? = null

    private var stateItem: MenuItem? = null

    @Inject
    lateinit var avatars: AvatarLoader

    @Inject
    lateinit var store: IssueStore

    @Inject
    lateinit var refreshIssueTaskFactory: RefreshIssueTaskFactory

    @Inject
    lateinit var labelsTaskFactory: EditLabelsTaskFactory

    @Inject
    lateinit var milestoneTaskFactory: EditMilestoneTaskFactory

    @Inject
    lateinit var assigneeTaskFactory: EditAssigneeTaskFactory

    @Inject
    lateinit var stateTaskFactory: EditStateTaskFactory

    @Inject
    lateinit var bodyImageGetter: HttpImageGetter

    @Inject
    lateinit var commentImageGetter: HttpImageGetter

    /**
     * Edit existing comment
     */
    private val editCommentListener: EditCommentListener = EditCommentListener { comment ->
        startActivityForResult(EditCommentActivity.createIntent(
                repositoryId, issueNumber, comment, user), COMMENT_EDIT)
    }

    /**
     * Delete existing comment
     */
    private val deleteCommentListener = DeleteCommentListener{ comment ->
        val args = Bundle()
        args.putParcelable(EXTRA_COMMENT, comment)
        ConfirmDialogFragment.show(
                activity,
                COMMENT_DELETE,
                activity!!
                        .getString(R.string.confirm_comment_delete_title),
                activity!!.getString(
                        R.string.confirm_comment_delete_message), args)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        repositoryId = InfoUtils.createRepoFromData(
                args!!.getString(EXTRA_REPOSITORY_OWNER),
                args.getString(EXTRA_REPOSITORY_NAME))
        issueNumber = args.getInt(EXTRA_ISSUE_NUMBER)
        user = args.getParcelable(EXTRA_USER)
        canWrite = args.getBoolean(EXTRA_CAN_WRITE_REPO, false)

        val dialogActivity = activity as BaseActivity?

        milestoneTask = milestoneTaskFactory.create(dialogActivity, repositoryId, issueNumber, createObserver())
        labelsTask = labelsTaskFactory.create(dialogActivity, repositoryId, issueNumber, createObserver())
        assigneeTask = assigneeTaskFactory.create(dialogActivity, repositoryId, issueNumber, createObserver())
        stateTask = stateTaskFactory.create(dialogActivity, repositoryId, issueNumber, createObserver())

        adapter.add(mainSection)
    }

    private fun createObserver(): Consumer<Issue> {
        return Consumer { issue ->
            updateHeader(issue)
            refreshIssue()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        issue = store.getIssue(repositoryId, issueNumber)

        if (issue == null || issue!!.comments()!! > 0 && items.isEmpty()) {
            mainSection.setFooter(LoadingItem(R.string.loading_comments))
        }

        if (issue != null && items.isNotEmpty()) {
            updateList(issue, items)
        } else {
            if (issue != null) {
                updateHeader(issue)
            }
            refreshIssue()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_comment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemDecoration = DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(resources.getDrawable(R.drawable.list_divider_5dp))

        list.layoutManager = LinearLayoutManager(activity)
        list.addItemDecoration(itemDecoration)
        list.adapter = adapter
    }

    private fun updateHeader(issue: Issue?) {
        if (!isAdded) {
            return
        }

        if (issue != null) {
            mainSection.setHeader(IssueHeaderItem(avatars, bodyImageGetter, requireContext(), this, issue))
        }

        pb_loading.visibility = GONE
        list.visibility = VISIBLE
        updateStateItem(issue)
    }

    private fun refreshIssue() {
        refreshIssueTaskFactory.create(repositoryId, issueNumber)
                .refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { (_, _, _) -> isAdded }
                .`as`(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe({ fullIssue ->
                    issue = fullIssue.issue
                    items.clear()
                    items.addAll(fullIssue.events)
                    items.addAll(fullIssue.comments)
                    updateList(fullIssue.issue, items)
                }, { _ ->
                    ToastUtils.show(activity, R.string.error_issue_load)
                    pb_loading.visibility = GONE
                })
    }

    private fun updateList(issue: Issue?, items: MutableList<Any>) {
        items.sortBy {
            when (it) {
                is GitHubComment -> it.createdAt()
                is GitHubEvent -> it.createdAt()
                is IssueEvent -> it.createdAt()
                else -> null
            }
        }

        val listItems = ArrayList<Item<*>>()
        for (item in items) {
            if (item is IssueEvent) {
                listItems.add(IssueEventItem(avatars, activity!!, issue!!, item))
            } else if (item is GitHubComment) {
                listItems.add(
                        GitHubCommentItem(avatars, commentImageGetter,
                                editCommentListener, deleteCommentListener,
                                AccountUtils.getLogin(activity), canWrite,
                                item
                        )
                )
            }
        }

        mainSection.removeFooter()
        mainSection.update(listItems)
        updateHeader(issue)
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, arguments: Bundle) {
        if (RESULT_OK != resultCode) {
            return
        }

        when (requestCode) {
            ISSUE_MILESTONE_UPDATE -> milestoneTask!!.edit(MilestoneDialogFragment.getSelected(arguments))
            ISSUE_ASSIGNEE_UPDATE -> assigneeTask!!.edit(AssigneeDialogFragment.getSelected(arguments))
            ISSUE_LABELS_UPDATE -> {
                val labels = LabelsDialogFragment.getSelected(arguments)
                labelsTask!!.edit(labels)
            }
            ISSUE_CLOSE -> stateTask!!.edit(true)
            ISSUE_REOPEN -> stateTask!!.edit(false)
            COMMENT_DELETE -> {
                val comment = arguments.getParcelable<GitHubComment>(EXTRA_COMMENT)

                ServiceGenerator.createService(activity, IssueCommentService::class.java)
                        .deleteIssueComment(repositoryId!!.owner()!!.login(), repositoryId!!.name(), comment!!.id()!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(RxProgress.bindToLifecycle(activity, R.string.deleting_comment))
                        .`as`(AutoDisposeUtils.bindToLifecycle(this))
                        .subscribe({ _ ->
                            if (items.isNotEmpty()) {
                                val commentPosition = findCommentPositionInItems(comment)
                                if (commentPosition >= 0) {
                                    issue = issue!!.toBuilder()
                                            .comments(issue!!.comments()!! - 1)
                                            .build()

                                    items.removeAt(commentPosition)
                                    updateList(issue, items)
                                }
                            } else {
                                refreshIssue()
                            }
                        }, { e ->
                            Log.d(TAG, "Exception deleting comment on issue", e)
                            ToastUtils.show(activity, e.message)
                        })
            }
        }
    }

    private fun updateStateItem(issue: Issue?) {
        if (issue != null && stateItem != null) {
            if (IssueState.Open == issue.state()) {
                stateItem!!.setTitle(R.string.close)
                stateItem!!.setIcon(R.drawable.ic_github_issue_closed_white_24dp)
            } else {
                stateItem!!.setTitle(R.string.reopen)
                stateItem!!.setIcon(R.drawable.ic_github_issue_reopened_white_24dp)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val editItem = menu.findItem(R.id.m_edit)
        val stateItem = menu.findItem(R.id.m_state)
        if (editItem != null && stateItem != null) {
            var isCreator = false
            if (issue != null) {
                isCreator = issue!!.user()!!.login() == AccountUtils.getLogin(activity)
            }
            editItem.isVisible = canWrite || isCreator
            stateItem.isVisible = canWrite || isCreator
        }
        updateStateItem(issue)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_issue_view, menu)
        stateItem = menu.findItem(R.id.m_state)
        updateStateItem(issue)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (RESULT_OK != resultCode || data == null) {
            return
        }

        when (requestCode) {
            ISSUE_EDIT -> {
                val editedIssue = data.getParcelableExtra<Issue>(EXTRA_ISSUE)
                bodyImageGetter.encode(editedIssue.id(), editedIssue.bodyHtml())
                updateHeader(editedIssue)
                return
            }
            COMMENT_CREATE -> {
                val comment: GitHubComment? = data.getParcelableExtra(EXTRA_COMMENT)
                if (comment != null) {
                    items += comment
                    issue = issue!!.toBuilder()
                            .comments(issue!!.comments()!! + 1)
                            .build()
                    updateList(issue, items)
                }
                return
            }
            COMMENT_EDIT -> {
                val comment = data.getParcelableExtra<GitHubComment>(EXTRA_COMMENT)
                if (comment != null && items.isNotEmpty()) {
                    val commentPosition = findCommentPositionInItems(comment)
                    if (commentPosition >= 0) {
                        commentImageGetter.removeFromCache(comment.id())
                        replaceCommentInItems(commentPosition, comment)
                        updateList(issue, items)
                    }
                } else {
                    refreshIssue()
                }
            }
        }
    }

    private fun shareIssue() {
        val id = InfoUtils.createRepoId(repositoryId)
        if (IssueUtils.isPullRequest(issue)) {
            startActivity(ShareUtils.create("Pull Request " + issueNumber
                    + " on " + id, "https://github.com/" + id + "/pull/"
                    + issueNumber))
        } else {
            startActivity(ShareUtils
                    .create("Issue $issueNumber on $id",
                            "https://github.com/" + id + "/issues/"
                                    + issueNumber))
        }
    }

    private fun openPullRequestCommits() {
        if (IssueUtils.isPullRequest(issue)) {
            val pullRequest = issue!!.pullRequest()

            val base = pullRequest!!.base()!!.sha()
            val head = pullRequest.head()!!.sha()
            val repo = pullRequest.base()!!.repo()
            startActivity(CommitCompareViewActivity.createIntent(repo, base, head))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_edit -> {
                if (issue != null) {
                    startActivityForResult(EditIssueActivity.createIntent(issue,
                            repositoryId!!.owner()!!.login(), repositoryId!!.name(), user),
                            ISSUE_EDIT)
                }
                return true
            }
            R.id.m_comment -> {
                if (issue != null) {
                    startActivityForResult(CreateCommentActivity.createIntent(
                            repositoryId, issueNumber, user), COMMENT_CREATE)
                }
                return true
            }
            R.id.m_refresh -> {
                refreshIssue()
                return true
            }
            R.id.m_share -> {
                if (issue != null) {
                    shareIssue()
                }
                return true
            }
            R.id.m_state -> {
                if (issue != null) {
                    stateTask!!.confirm(IssueState.Open == issue!!.state())
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Finds the position of the given comment in the list of this issue's items.
     *
     * @param comment The comment to look for.
     * @return The position of the comment in the list, or -1 if not found.
     */
    private fun findCommentPositionInItems(comment: GitHubComment): Int {
        val id = comment.id()
        return items.indexOfFirst {
            if (it is GitHubComment) {
                return@indexOfFirst it.id()!! == id
            }
            return@indexOfFirst false
        }
    }

    /**
     * Replaces a comment in the list by another
     *
     * @param commentPosition The position of the comment in the list
     * @param comment         The comment to replace
     * @return True if successfully removed, false otherwise.
     */
    private fun replaceCommentInItems(commentPosition: Int, comment: GitHubComment): Boolean {
        val item = items[commentPosition]
        if (item is GitHubComment) {
            items[commentPosition] = comment
            return true
        }
        return false
    }

    override fun onCommitsClicked() {
        if (IssueUtils.isPullRequest(issue)) {
            openPullRequestCommits()
        }
    }

    override fun onStateClicked() {
        if (issue != null) {
            stateTask!!.confirm(IssueState.Open == issue!!.state())
        }
    }

    override fun onMilestonesClicked() {
        if (issue != null && canWrite) {
            milestoneTask!!.prompt(issue!!.milestone())
        }
    }

    override fun onAssigneesClicked() {
        if (issue != null && canWrite) {
            assigneeTask!!.prompt(issue!!.assignee())
        }
    }

    override fun onLabelsClicked() {
        if (issue != null && canWrite) {
            labelsTask!!.prompt(issue!!.labels())
        }
    }

    companion object {
        private val TAG = "IssueFragment"
    }
}
