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
package com.github.pockethub.android.ui.commit

import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.github.pockethub.android.Intents.EXTRA_BASE
import com.github.pockethub.android.Intents.EXTRA_COMMENT
import com.github.pockethub.android.Intents.EXTRA_REPOSITORY
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.COMMENT_CREATE
import com.github.pockethub.android.core.commit.CommitStore
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.core.commit.FullCommitFile
import com.github.pockethub.android.core.commit.RefreshCommitTaskFactory
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.LoadingItem
import com.github.pockethub.android.ui.item.TextItem
import com.github.pockethub.android.ui.item.commit.CommitCommentItem
import com.github.pockethub.android.ui.item.commit.CommitFileHeaderItem
import com.github.pockethub.android.ui.item.commit.CommitFileLineItem
import com.github.pockethub.android.ui.item.commit.CommitHeaderItem
import com.github.pockethub.android.ui.item.commit.CommitParentItem
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.HttpImageGetter
import com.github.pockethub.android.util.InfoUtils
import com.github.pockethub.android.util.ShareUtils
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.Commit
import com.meisolsson.githubsdk.model.GitHubFile
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.git.GitComment
import com.meisolsson.githubsdk.model.git.GitCommit
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.diff_line_dialog.view.*
import kotlinx.android.synthetic.main.fragment_commit_diff_list.*
import java.util.ArrayList
import java.util.Collections
import javax.inject.Inject

/**
 * Fragment to display commit details with diff output
 */
class CommitDiffListFragment : BaseFragment(), OnItemClickListener {
    
    @Inject
    lateinit var refreshCommitTaskFactory: RefreshCommitTaskFactory

    @Inject
    lateinit var commentImageGetter: HttpImageGetter

    @Inject
    lateinit var avatars: AvatarLoader

    @Inject
    lateinit var store: CommitStore

    private var diffStyler: DiffStyler? = null

    private var repository: Repository? = null

    private var base: String? = null

    private var commit: Commit? = null

    private var comments: MutableList<GitComment>? = null

    private var files: List<FullCommitFile>? = null

    private val adapter = GroupAdapter<ViewHolder>()

    private val mainSection = Section()

    private val commitSection = Section()

    private val filesSection = Section()

    private val commentSection = Section()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments!!
        base = args.getString(EXTRA_BASE)
        repository = args.getParcelable(EXTRA_REPOSITORY)

        mainSection.add(commitSection)
        mainSection.add(filesSection)
        mainSection.add(commentSection)

        adapter.add(mainSection)
        adapter.setOnItemClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        commit = store.getCommit(repository, base)

        if (files == null || commit != null && commit!!.commit()!!.commentCount()!! > 0 && comments == null) {
            mainSection.setFooter(LoadingItem(R.string.loading_files_and_comments))
        }

        if (commit != null && comments != null && files != null) {
            updateList(commit!!, comments!!, files!!)
        } else {
            if (commit != null) {
                updateHeader(commit!!)
            }
            refreshCommit()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        diffStyler = DiffStyler(resources)

        list.layoutManager = LinearLayoutManager(activity)
        list.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_commit_diff_list, container, false)
    }

    private fun addComment(comment: GitComment) {
        if (comments != null && files != null) {
            comments!!.add(comment)
            var rawCommit: GitCommit? = commit!!.commit()
            if (rawCommit != null) {
                rawCommit = rawCommit.toBuilder()
                    .commentCount(rawCommit.commentCount()!! + 1)
                    .build()

                commit = commit!!.toBuilder()
                    .commit(rawCommit)
                    .build()
            }
            commentImageGetter.encode(comment, comment.bodyHtml())
            updateItems(comments!!, files!!)
        } else {
            refreshCommit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (RESULT_OK == resultCode && COMMENT_CREATE == requestCode
            && data != null) {
            val comment = data.getParcelableExtra<GitComment>(EXTRA_COMMENT)
            addComment(comment)
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(optionsMenu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_commit_view, optionsMenu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (!isAdded) {
            return false
        }

        when (item!!.itemId) {
            R.id.m_refresh -> {
                refreshCommit()
                return true
            }
            R.id.m_copy_hash -> {
                copyHashToClipboard()
                return true
            }
            R.id.m_comment -> {
                startActivityForResult(
                    CreateCommentActivity.createIntent(repository, base),
                    COMMENT_CREATE)
                return true
            }
            R.id.m_share -> {
                shareCommit()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun copyHashToClipboard() {
        val manager = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("hash", commit!!.sha())
        manager.primaryClip = clip
        Toast.makeText(activity, R.string.toast_msg_copied, Toast.LENGTH_SHORT).show()
    }

    private fun shareCommit() {
        val id = InfoUtils.createRepoId(repository!!)
        startActivity(ShareUtils.create(
            "Commit " + CommitUtils.abbreviate(base) + " on " + id,
            "https://github.com/$id/commit/$base")
        )
    }

    private fun refreshCommit() {
        refreshCommitTaskFactory.create(activity, repository, base)
            .refresh()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDisposeUtils.bindToLifecycle(this))
            .subscribe({ full ->
                val files = full.commit.files()
                diffStyler!!.setFiles(files)
                if (files != null) {
                    Collections.sort(files, CommitFileComparator())
                }

                updateList(full.commit, full, full.files)
            }, { e ->
                ToastUtils.show(activity, R.string.error_commit_load)
                pb_loading.visibility = View.GONE
            })
    }

    private fun addCommitParents(commit: Commit) {
        val parents = commit.parents()
        if (parents == null || parents.isEmpty()) {
            return
        }

        val items = ArrayList<CommitParentItem>()
        for (parent in parents) {
            items.add(CommitParentItem(activity!!, parent))
        }
        commitSection.update(items)
    }

    private fun updateHeader(commit: Commit) {
        pb_loading.visibility = View.GONE
        list.visibility = View.VISIBLE

        mainSection.setHeader(CommitHeaderItem(avatars, activity!!, commit))
        addCommitParents(commit)
    }

    private fun updateList(commit: Commit, comments: MutableList<GitComment>, files: List<FullCommitFile>) {
        if (!isAdded) {
            return
        }

        this.commit = commit
        this.comments = comments
        this.files = files

        updateHeader(commit)
        mainSection.removeFooter()

        filesSection.setHeader(
            TextItem(R.layout.commit_file_details_header,
                R.id.tv_commit_file_summary, CommitUtils.formatStats(commit.files()))
        )
        updateItems(comments, files)
    }

    private fun updateItems(comments: List<GitComment>, files: List<FullCommitFile>) {
        filesSection.update(createFileSections(files))

        val items = ArrayList<CommitCommentItem>()
        for (comment in comments) {
            items.add(CommitCommentItem(avatars, commentImageGetter, comment))
        }
        commentSection.update(items)
    }

    private fun createFileSections(files: List<FullCommitFile>): List<Section> {
        val sections = ArrayList<Section>()
        for (file in files) {
            val section = Section(CommitFileHeaderItem(activity!!, file.file))
            val lines = diffStyler!!.get(file.file.filename())
            for ((number, line) in lines.withIndex()) {
                section.add(CommitFileLineItem(diffStyler!!, line))
                for (comment in file.get(number)) {
                    section.add(CommitCommentItem(avatars, commentImageGetter, comment, true))
                }
            }

            sections.add(section)
        }

        return sections
    }

    private fun showFileOptions(line: CharSequence, position: Int, file: GitHubFile) {

        val builder = MaterialDialog.Builder(activity!!)
            .title(CommitUtils.getName(file)!!)

        val dialogHolder = arrayOfNulls<MaterialDialog>(1)

        val view = activity!!.layoutInflater.inflate(R.layout.diff_line_dialog, null)

        val diff = view.tv_diff
        diff.text = line
        diffStyler!!.updateColors(line, diff)

        view.tv_commit.text = getString(R.string.commit_prefix) + CommitUtils.abbreviate(commit)!!

        view.ll_view_area.setOnClickListener { v ->
            dialogHolder[0]!!.dismiss()
            openFile(file)
        }

        view.ll_comment_area.setOnClickListener { v ->
            dialogHolder[0]!!.dismiss()
            startActivityForResult(CreateCommentActivity
                .createIntent(repository, commit!!.sha(),
                    file.filename(), position),
                COMMENT_CREATE)
        }

        builder.customView(view, false)
            .negativeText(R.string.cancel)
            .onNegative { dialog, which -> dialog.dismiss() }

        val dialog = builder.build()
        dialogHolder[0] = dialog
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    private fun openFile(file: GitHubFile) {
        if (!TextUtils.isEmpty(file.filename()) && !TextUtils.isEmpty(file.sha())) {
            startActivity(CommitFileViewActivity.createIntent(repository!!, base!!, file))
        }
    }

    /**
     * Select previous file by scanning backwards from the current position
     *
     * @param position
     * @param item
     * @param adapter
     */
    private fun selectPreviousFile(position: Int, item: Any, adapter: GroupAdapter<*>) {
        var posItem: Item<*>
        var pos = position
        var line: CharSequence?
        line = item as? CharSequence

        var linePosition = 0
        while (--pos >= 0) {
            posItem = adapter.getItem(pos)

            if (posItem is CommitFileHeaderItem) {
                if (line != null) {
                    showFileOptions(line, linePosition, posItem.file)
                }
                break
            } else if (posItem is CharSequence) {
                if (line != null) {
                    linePosition++
                } else {
                    line = posItem
                }
            }
        }
    }

    override fun onItemClick(@NonNull item: Item<*>, @NonNull view: View) {
        val position = adapter.getAdapterPosition(item)

        if (item is CommitParentItem) {
            val sha = item.commit.sha()
            startActivity(CommitViewActivity.createIntent(repository!!, sha!!))
        } else if (item is CommitFileHeaderItem) {
            openFile(item.file)
        } else if (item is CharSequence) {
            selectPreviousFile(position, item, adapter)
        } else if (item is CommitCommentItem) {
            if (!TextUtils.isEmpty(item.comment.path())) {
                selectPreviousFile(position, item, adapter)
            }
        }
    }
}
