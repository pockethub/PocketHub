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
package com.github.pockethub.android.ui.gist

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.pockethub.android.Intents.EXTRA_COMMENT
import com.github.pockethub.android.Intents.EXTRA_GIST_ID
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.COMMENT_CREATE
import com.github.pockethub.android.RequestCodes.COMMENT_DELETE
import com.github.pockethub.android.RequestCodes.COMMENT_EDIT
import com.github.pockethub.android.accounts.AccountUtils
import com.github.pockethub.android.core.OnLoadListener
import com.github.pockethub.android.core.gist.GistStore
import com.github.pockethub.android.core.gist.RefreshGistTaskFactory
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.rx.RxProgress
import com.github.pockethub.android.ui.ConfirmDialogFragment
import com.github.pockethub.android.ui.DialogResultListener
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.comment.DeleteCommentListener
import com.github.pockethub.android.ui.comment.EditCommentListener
import com.github.pockethub.android.ui.item.GitHubCommentItem
import com.github.pockethub.android.ui.item.LoadingItem
import com.github.pockethub.android.ui.item.gist.GistFileItem
import com.github.pockethub.android.ui.item.gist.GistHeaderItem
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.github.pockethub.android.util.ShareUtils
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Gist
import com.meisolsson.githubsdk.model.GitHubComment
import com.meisolsson.githubsdk.service.gists.GistCommentService
import com.meisolsson.githubsdk.service.gists.GistService
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_comment_list.*
import java.util.ArrayList
import java.util.Collections
import javax.inject.Inject

/**
 * Activity to display an existing Gist
 */
class GistFragment : BaseFragment(), OnItemClickListener, DialogResultListener {

    @Inject
    lateinit var store: GistStore

    @Inject
    lateinit var refreshGistTaskFactory: RefreshGistTaskFactory

    @Inject
    lateinit var imageGetter: HttpImageGetter

    @Inject
    lateinit var avatars: AvatarLoader

    private var gistId: String? = null

    private var comments: MutableList<GitHubComment>? = null

    private var gist: Gist? = null

    private val adapter = GroupAdapter<ViewHolder>()

    private val mainSection = Section()

    private val filesSection = Section()

    private val commentsSection = Section()

    private var starred: Boolean = false

    private var loadFinished: Boolean = false

    private val isOwner: Boolean
        get() {
            if (gist == null) {
                return false
            }
            val user = gist!!.owner() ?: return false
            val login = AccountUtils.getLogin(activity)
            return login != null && login == user.login()
        }

    /**
     * Edit existing comment
     */
    private val editCommentListener: EditCommentListener = EditCommentListener { comment ->
        startActivityForResult(
            EditCommentActivity.createIntent(gist, comment),
            COMMENT_EDIT)
    }

    /**
     * Delete existing comment
     */
    private val deleteCommentListener = DeleteCommentListener { comment ->
        val args = Bundle()
        args.putParcelable(EXTRA_COMMENT, comment)
        ConfirmDialogFragment.show(
            activity,
            COMMENT_DELETE,
            activity!!.getString(R.string.confirm_comment_delete_title),
            activity!!.getString(R.string.confirm_comment_delete_message),
            args
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gistId = arguments!!.getString(EXTRA_GIST_ID)
        gist = store.getGist(gistId)

        mainSection.add(filesSection)
        mainSection.add(commentsSection)
        adapter.add(mainSection)

        adapter.setOnItemClickListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_comment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(resources.getDrawable(R.drawable.list_divider_5dp))

        list.layoutManager = LinearLayoutManager(activity)
        list.addItemDecoration(itemDecoration)
        list.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (gist != null) {
            updateHeader(gist)
            updateFiles(gist)
        }

        if (gist == null || gist!!.comments()!! > 0 && comments == null) {
            mainSection.setFooter(LoadingItem(R.string.loading_comments))
        }

        if (gist != null && comments != null) {
            updateList(gist, comments!!)
        } else {
            refreshGist()
        }
    }

    private fun updateHeader(gist: Gist?) {
        mainSection.setHeader(GistHeaderItem(activity!!, gist!!))
        pb_loading.visibility = GONE
        list.visibility = VISIBLE
    }

    override fun onCreateOptionsMenu(options: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_gist_view, options)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val owner = isOwner
        if (!owner) {
            menu!!.removeItem(R.id.m_delete)
            val starItem = menu.findItem(R.id.m_star)
            starItem.isEnabled = loadFinished && !owner
            if (starred) {
                starItem.setTitle(R.string.unstar)
            } else {
                starItem.setTitle(R.string.star)
            }
        } else {
            menu!!.removeItem(R.id.m_star)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (gist == null) {
            return super.onOptionsItemSelected(item)
        }

        when (item!!.itemId) {
            R.id.m_comment -> {
                startActivityForResult(CreateCommentActivity.createIntent(gist),
                    COMMENT_CREATE)
                return true
            }
            R.id.m_star -> {
                if (starred) {
                    unstarGist()
                } else {
                    starGist()
                }
                return true
            }
            R.id.m_refresh -> {
                refreshGist()
                return true
            }
            R.id.m_share -> {
                shareGist()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun starGist() {
        ToastUtils.show(activity, R.string.starring_gist)
        ServiceGenerator.createService(activity, GistService::class.java)
            .starGist(gistId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDisposeUtils.bindToLifecycle(this))
            .subscribe({ response -> starred = response.code() == 204 },
                { e -> ToastUtils.show(context as Activity, e.message) })
    }

    private fun shareGist() {
        val subject = StringBuilder("Gist ")
        val id = gist!!.id()
        subject.append(id)
        val user = gist!!.owner()
        if (user != null && !TextUtils.isEmpty(user.login())) {
            subject.append(" by ").append(user.login())
        }
        startActivity(ShareUtils.create(subject, "https://gist.github.com/$id"))
    }

    private fun unstarGist() {
        ToastUtils.show(activity, R.string.unstarring_gist)
        ServiceGenerator.createService(activity, GistService::class.java)
            .unstarGist(gistId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDisposeUtils.bindToLifecycle(this))
            .subscribe({ response -> starred = response.code() != 204 },
                { e -> ToastUtils.show(context as Activity, e.message) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (RESULT_OK != resultCode || data == null) {
            return
        }

        when (requestCode) {
            COMMENT_CREATE -> {
                val comment = data.getParcelableExtra<GitHubComment>(EXTRA_COMMENT)
                if (comments != null) {
                    comments!!.add(comment)
                    gist = gist!!.toBuilder().comments(gist!!.comments()!! + 1).build()
                    updateList(gist, comments!!)
                } else {
                    refreshGist()
                }
                return
            }
            COMMENT_EDIT -> {
                val comment = data.getParcelableExtra<GitHubComment>(EXTRA_COMMENT)
                if (comments != null && comment != null) {
                    val position = Collections.binarySearch(comments, comment) {
                        lhs, rhs -> lhs.id()!!.compareTo(rhs.id()!!)
                    }
                    imageGetter.removeFromCache(comment.id())
                    comments!![position] = comment
                    updateList(gist, comments!!)
                } else {
                    refreshGist()
                }
                return
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateFiles(gist: Gist?) {
        if (activity == null) {
            return
        }

        val files = gist!!.files()
        if (files == null || files.isEmpty()) {
            filesSection.update(emptyList())
            return
        }

        val fileItems = ArrayList<GistFileItem>()
        for (file in files.values) {
            fileItems.add(GistFileItem(file))
        }
        filesSection.update(fileItems)
    }

    private fun updateList(gist: Gist?, comments: List<GitHubComment>) {
        val items = ArrayList<GitHubCommentItem>()
        val username = AccountUtils.getLogin(activity)
        val isOwner = isOwner

        for (comment in comments) {
            items.add(
                GitHubCommentItem(avatars, imageGetter, editCommentListener,
                    deleteCommentListener, username!!, isOwner, comment)
            )
        }
        commentsSection.update(items)
        mainSection.removeFooter()

        updateHeader(gist)
        updateFiles(gist)
    }

    private fun refreshGist() {
        refreshGistTaskFactory.create(activity, gistId)
            .refresh()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .filter { (gist1, starred1, comments1) -> isAdded }
            .`as`(AutoDisposeUtils.bindToLifecycle(this))
            .subscribe({ fullGist ->
                val activity = activity
                if (activity is OnLoadListener<*>) {
                    (activity as OnLoadListener<Gist>).loaded(fullGist.gist)
                }

                starred = fullGist.starred
                loadFinished = true
                gist = fullGist.gist
                comments = fullGist.comments.toMutableList()
                updateList(fullGist.gist, fullGist.comments)
            }, { e -> ToastUtils.show(activity, R.string.error_gist_load) })
    }

    override fun onItemClick(@NonNull item: Item<*>, @NonNull view: View) {
        if (item is GistFileItem) {
            val position = adapter.getAdapterPosition(item)
            startActivity(GistFilesViewActivity.createIntent(gist!!, position - 1))
        }
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, arguments: Bundle) {
        if (RESULT_OK != resultCode) {
            return
        }

        when (requestCode) {
            COMMENT_DELETE -> {
                val comment = arguments.getParcelable<GitHubComment>(EXTRA_COMMENT)
                ServiceGenerator.createService(activity, GistCommentService::class.java)
                    .deleteGistComment(gistId, comment.id()!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(RxProgress.bindToLifecycle(activity, R.string.deleting_comment))
                    .`as`(AutoDisposeUtils.bindToLifecycle(this))
                    .subscribe({ response ->
                        // Update comment list
                        if (comments != null) {
                            val position = Collections.binarySearch(comments,
                                comment) { lhs, rhs -> lhs.id()!!.compareTo(rhs.id()!!) }
                            comments!!.removeAt(position)
                            updateList(gist, comments!!)
                        } else {
                            refreshGist()
                        }
                    }, { e ->
                        Log.d(TAG, "Exception deleting comment on gist", e)
                        ToastUtils.show(context as Activity, e.message)
                    })
            }
        }
    }

    companion object {

        private val TAG = "GistFragment"
    }
}
