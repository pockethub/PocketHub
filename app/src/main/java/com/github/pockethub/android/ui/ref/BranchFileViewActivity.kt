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
package com.github.pockethub.android.ui.ref

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.github.pockethub.android.Intents.*
import com.github.pockethub.android.R
import com.github.pockethub.android.core.commit.CommitUtils
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.ui.MarkdownLoader
import com.github.pockethub.android.util.*
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.git.GitBlob
import com.meisolsson.githubsdk.service.git.GitService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import javax.inject.Inject
import com.github.pockethub.android.util.PreferenceUtils.RENDER_MARKDOWN
import com.github.pockethub.android.util.PreferenceUtils.WRAP
import kotlinx.android.synthetic.main.activity_commit_file_view.*

/**
 * Activity to view a file on a branch
 */
class BranchFileViewActivity : BaseActivity() {

    @Inject
    lateinit var avatars: AvatarLoader

    @Inject
    lateinit var imageGetter: HttpImageGetter

    private var repo: Repository? = null

    private var sha: String? = null

    private var path: String? = null

    private var file: String? = null

    private var branch: String? = null

    private var isMarkdownFile: Boolean = false

    private var renderedMarkdown: String? = null

    private var blob: GitBlob? = null

    private lateinit var editor: SourceEditor

    private var markdownItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commit_file_view)

        repo = getParcelableExtra(EXTRA_REPOSITORY)
        sha = getStringExtra(EXTRA_BASE)
        path = getStringExtra(EXTRA_PATH)
        branch = getStringExtra(EXTRA_HEAD)

        wv_code.settings.builtInZoomControls = true
        wv_code.settings.useWideViewPort = true

        file = CommitUtils.getName(path)
        isMarkdownFile = MarkdownUtils.isMarkdown(file)
        editor = SourceEditor(wv_code)
        editor.wrap = PreferenceUtils.getCodePreferences(this).getBoolean(WRAP, false)

        val actionBar = supportActionBar!!
        actionBar.title = file
        actionBar.subtitle = branch
        avatars.bind(actionBar, repo!!.owner()!!)

        loadContent()
    }

    override fun onCreateOptionsMenu(optionsMenu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_file_view, optionsMenu)

        val wrapItem = optionsMenu.findItem(R.id.m_wrap)
        if (PreferenceUtils.getCodePreferences(this).getBoolean(WRAP, false)) {
            wrapItem.setTitle(R.string.disable_wrapping)
        } else {
            wrapItem.setTitle(R.string.enable_wrapping)
        }

        markdownItem = optionsMenu.findItem(R.id.m_render_markdown)
        if (isMarkdownFile) {
            markdownItem!!.isEnabled = blob != null
            markdownItem!!.isVisible = true
            if (PreferenceUtils.getCodePreferences(this).getBoolean(
                    RENDER_MARKDOWN, true)) {
                markdownItem!!.setTitle(R.string.show_raw_markdown)
            } else {
                markdownItem!!.setTitle(R.string.render_markdown)
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_wrap -> {
                if (editor.wrap) {
                    item.setTitle(R.string.enable_wrapping)
                } else {
                    item.setTitle(R.string.disable_wrapping)
                }
                editor.toggleWrap()
                PreferenceUtils.save(PreferenceUtils.getCodePreferences(this)
                    .edit().putBoolean(WRAP, editor.wrap))
                return true
            }

            R.id.m_share -> {
                shareFile()
                return true
            }

            R.id.m_render_markdown -> {
                if (editor.isMarkdown) {
                    item.setTitle(R.string.render_markdown)
                    editor.toggleMarkdown()
                    editor.setSource(file, blob)
                } else {
                    item.setTitle(R.string.show_raw_markdown)
                    editor.toggleMarkdown()
                    if (renderedMarkdown != null) {
                        editor.setSource(file, renderedMarkdown, false)
                    } else {
                        loadMarkdown()
                    }
                }
                PreferenceUtils.save(PreferenceUtils.getCodePreferences(this)
                    .edit().putBoolean(RENDER_MARKDOWN, editor.isMarkdown))
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun shareFile() {
        val id = InfoUtils.createRepoId(repo!!)
        startActivity(ShareUtils.create("$path at $branch on $id",
            "https://github.com/" + id + "/blob/" + branch + '/'.toString() + path))
    }

    private fun loadMarkdown() {
        pb_loading.visibility = View.VISIBLE
        wv_code.visibility = View.GONE

        val markdown = String(Base64.decode(blob!!.content(), Base64.DEFAULT))
        MarkdownLoader.load(this, markdown, repo, imageGetter, false)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ rendered ->
                pb_loading.visibility = View.GONE
                wv_code.visibility = View.VISIBLE

                if (!TextUtils.isEmpty(rendered)) {
                    renderedMarkdown = rendered.toString()
                    if (markdownItem != null) {
                        markdownItem!!.isEnabled = true
                    }
                    editor!!.setMarkdown(true).setSource(file, renderedMarkdown, false)
                }
            }, { e -> ToastUtils.show(this, R.string.error_rendering_markdown) })
    }

    private fun loadContent() {
        pb_loading.visibility = View.VISIBLE
        wv_code.visibility = View.GONE

        ServiceGenerator.createService(this, GitService::class.java)
            .getGitBlob(repo!!.owner()!!.login(), repo!!.name(), sha)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDisposeUtils.bindToLifecycle(this))
            .subscribe({ response ->
                blob = response.body()

                if (markdownItem != null) {
                    markdownItem!!.isEnabled = true
                }

                if (isMarkdownFile && PreferenceUtils.getCodePreferences(this).getBoolean(
                        RENDER_MARKDOWN, true)) {
                    loadMarkdown()
                } else {
                    pb_loading.visibility = View.GONE
                    wv_code.visibility = View.VISIBLE

                    editor.setMarkdown(false).setSource(file, blob)
                }
            }, { e ->
                Log.d(TAG, "Loading file contents failed", e)

                pb_loading.visibility = View.GONE
                wv_code.visibility = View.VISIBLE
                ToastUtils.show(this, R.string.error_file_load)
            })
    }

    companion object {

        private val TAG = "BranchFileViewActivity"

        private val ARG_TEXT = "text"

        private val ARG_REPO = "repo"

        /**
         * Create intent to show file in commit
         *
         * @param repository
         * @param branch
         * @param file
         * @param blobSha
         * @return intent
         */
        fun createIntent(repository: Repository, branch: String,
            file: String, blobSha: String): Intent {
            val builder = Builder("branch.file.VIEW")
            builder.repo(repository)
            builder.add(EXTRA_BASE, blobSha)
            builder.add(EXTRA_PATH, file)
            builder.add(EXTRA_HEAD, branch)
            return builder.toIntent()
        }
    }
}
