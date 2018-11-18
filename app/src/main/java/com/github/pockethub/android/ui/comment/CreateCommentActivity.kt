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
package com.github.pockethub.android.ui.comment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.github.pockethub.android.Intents.EXTRA_COMMENT
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.ui.PagerHandler
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubComment
import com.meisolsson.githubsdk.model.git.GitComment
import kotlinx.android.synthetic.main.pager_with_tabs.*
import javax.inject.Inject

/**
 * Base activity for creating comments
 */
abstract class CreateCommentActivity : BaseActivity() {

    private var applyItem: MenuItem? = null

    /**
     * Avatar loader
     */
    @Inject
    lateinit var avatars: AvatarLoader

    private var pagerHandler: PagerHandler<CommentPreviewPagerAdapter>? = null

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        configurePager()
    }

    private fun configurePager() {
        val adapter = createAdapter()
        pagerHandler = PagerHandler(this, vp_pages, adapter)
        pagerHandler!!.onPagedChanged = this::onPageChanged
        lifecycle.addObserver(pagerHandler!!)
        pagerHandler!!.tabs = sliding_tabs_layout
    }

    private fun onPageChanged(position: Int) {
        pagerHandler!!.adapter.setCurrentItem(position)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(pagerHandler!!)
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
        applyItem?.isEnabled =
            pagerHandler?.adapter != null && pagerHandler?.adapter!!.commentText.isNotEmpty()
    }

    /**
     * Create comment
     *
     * @param comment
     */
    protected abstract fun createComment(comment: String)

    /**
     * Finish this activity passing back the created comment
     *
     * @param comment
     */
    protected fun finish(comment: GitHubComment) {
        val data = Intent()
        data.putExtra(EXTRA_COMMENT, comment)
        setResult(RESULT_OK, data)
        finish()
    }

    protected fun finish(comment: GitComment) {
        val data = Intent()
        data.putExtra(EXTRA_COMMENT, comment)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.m_apply -> {
                createComment(pagerHandler!!.adapter.commentText)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    protected open fun createAdapter(): CommentPreviewPagerAdapter {
        return CommentPreviewPagerAdapter(this, null)
    }

    // For some reason we can't call the super method since that makes invalidateOptionsMenu weird
    @SuppressLint("MissingSuperCall")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_comment, menu)
        applyItem = menu.findItem(R.id.m_apply)
        return true
    }
}
