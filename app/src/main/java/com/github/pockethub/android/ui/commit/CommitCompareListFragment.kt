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

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.pockethub.android.Intents.EXTRA_BASE
import com.github.pockethub.android.Intents.EXTRA_HEAD
import com.github.pockethub.android.Intents.EXTRA_REPOSITORY
import com.github.pockethub.android.R
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.TextItem
import com.github.pockethub.android.ui.item.commit.CommitFileHeaderItem
import com.github.pockethub.android.ui.item.commit.CommitFileLineItem
import com.github.pockethub.android.ui.item.commit.CommitItem
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Commit
import com.meisolsson.githubsdk.model.CommitCompare
import com.meisolsson.githubsdk.model.GitHubFile
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.service.repositories.RepositoryCommitService
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_commit_diff_list.*
import java.text.MessageFormat
import java.util.ArrayList
import java.util.Collections
import javax.inject.Inject

/**
 * Fragment to display a list of commits being compared
 */
class CommitCompareListFragment : BaseFragment(), OnItemClickListener {

    @Inject
    lateinit var avatars: AvatarLoader

    private var diffStyler: DiffStyler? = null

    private var repository: Repository? = null

    private var base: String? = null

    private var head: String? = null

    private val adapter = GroupAdapter<ViewHolder>()

    private val mainSection = Section()

    private val commitsSection = Section()

    private val filesSection = Section()

    private var compare: CommitCompare? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        val activity = context as Activity?
        repository = activity!!.intent.getParcelableExtra(EXTRA_REPOSITORY)
        base = getStringExtra(EXTRA_BASE)!!.substring(0, 7)
        head = getStringExtra(EXTRA_HEAD)!!.substring(0, 7)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainSection.add(commitsSection)
        mainSection.add(filesSection)
        adapter.add(mainSection)

        adapter.setOnItemClickListener(this)

        diffStyler = DiffStyler(resources)
        compareCommits()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!isAdded) {
            return false
        }

        return when (item.itemId) {
            R.id.m_refresh -> {
                compareCommits()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun compareCommits() {
        ServiceGenerator.createService(activity, RepositoryCommitService::class.java)
            .compareCommits(repository!!.owner()!!.login(), repository!!.name(), base, head)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDisposeUtils.bindToLifecycle(this))
            .subscribe({ response ->
                val compareCommit = response.body()
                val files = compareCommit.files()
                diffStyler!!.setFiles(files)
                Collections.sort(files, CommitFileComparator())
                updateList(compareCommit)
            }, { error -> ToastUtils.show(activity, R.string.error_commits_load) })
    }

    private fun updateList(compare: CommitCompare) {
        if (!isAdded) {
            return
        }

        this.compare = compare

        pb_loading.visibility = View.GONE
        list.visibility = View.VISIBLE

        val commits = compare.commits()
        if (!commits.isEmpty()) {
            val comparingCommits = getString(R.string.comparing_commits)
            val text = MessageFormat.format(comparingCommits, commits.size)
            commitsSection.setHeader(
                TextItem(R.layout.commit_details_header, R.id.tv_commit_summary, text)
            )

            val items = ArrayList<CommitItem>()
            for (commit in commits) {
                items.add(CommitItem(avatars!!, commit))
            }

            commitsSection.update(items)
        }

        val files = compare.files()
        if (!files.isEmpty()) {
            filesSection.setHeader(
                TextItem(R.layout.commit_compare_file_details_header,
                    R.id.tv_commit_file_summary, CommitUtils.formatStats(files))
            )
            filesSection.update(createFileSections(files))
        }
    }

    private fun createFileSections(files: List<GitHubFile>): List<Section> {
        val sections = ArrayList<Section>()

        for (file in files) {
            val section = Section(CommitFileHeaderItem(activity!!, file))
            val lines = diffStyler!!.get(file.filename())
            for (line in lines) {
                section.add(CommitFileLineItem(diffStyler!!, line))
            }

            sections.add(section)
        }

        return sections
    }

    private fun openCommit(commit: Commit) {
        if (compare != null) {
            var commitPosition = 0
            val commits = compare!!.commits()
            for (candidate in commits) {
                if (commit === candidate) {
                    break
                } else {
                    commitPosition++
                }
            }
            if (commitPosition < commits.size) {
                val ids = arrayOfNulls<String>(commits.size) as Array<String>
                for (i in commits.indices) {
                    ids[i] = commits[i].sha()!!
                }
                startActivity(CommitViewActivity.createIntent(repository!!, commitPosition, *ids))
            }
        } else {
            startActivity(CommitViewActivity.createIntent(repository!!, commit.sha()!!))
        }
    }

    private fun openFile(file: GitHubFile) {
        if (!TextUtils.isEmpty(file.filename()) && !TextUtils.isEmpty(file.sha())) {
            startActivity(CommitFileViewActivity.createIntent(repository!!, head!!, file))
        }
    }

    private fun openLine(adapter: GroupAdapter<*>, position: Int) {
        var pos = position
        var item: Any
        while (--pos >= 0) {
            item = adapter.getItem(pos)
            if (item is CommitFileHeaderItem) {
                openFile(item.file)
                return
            }
        }
    }

    override fun onItemClick(@NonNull item: Item<*>, @NonNull view: View) {
        when (item) {
            is CommitItem -> openCommit(item.commit)
            is CommitFileHeaderItem -> openFile(item.file)
            is CommitFileLineItem -> {
                val position = adapter.getAdapterPosition(item)
                openLine(adapter, position)
            }
        }
    }
}
