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

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.github.pockethub.android.Intents.Builder
import com.github.pockethub.android.Intents.EXTRA_GIST
import com.github.pockethub.android.Intents.EXTRA_GIST_ID
import com.github.pockethub.android.Intents.EXTRA_GIST_IDS
import com.github.pockethub.android.Intents.EXTRA_POSITION
import com.github.pockethub.android.R
import com.github.pockethub.android.core.OnLoadListener
import com.github.pockethub.android.core.gist.GistStore
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.rx.RxProgress
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.ui.ConfirmDialogFragment
import com.github.pockethub.android.ui.MainActivity
import com.github.pockethub.android.ui.PagerHandler
import com.github.pockethub.android.ui.item.gist.GistItem
import com.github.pockethub.android.ui.user.UriLauncherActivity
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Gist
import com.meisolsson.githubsdk.service.gists.GistService
import com.xwray.groupie.Item
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_pager.*
import java.io.Serializable
import javax.inject.Inject

/**
 * Activity to display a collection of Gists in a pager
 */
class GistsViewActivity : BaseActivity(), OnLoadListener<Gist> {

    @Inject
    lateinit var store: GistStore

    @Inject
    lateinit var avatars: AvatarLoader

    private var gists: Array<String>? = null

    private var gist: Gist? = null

    private var initialPosition: Int = 0

    private var pagerHandler: PagerHandler<GistsPagerAdapter>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)

        gists = getStringArrayExtra(EXTRA_GIST_IDS)
        gist = getParcelableExtra(EXTRA_GIST)
        initialPosition = getIntExtra(EXTRA_POSITION)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Support opening this activity with a single Gist that may be present
        // in the intent but not currently present in the store
        if (gists == null && gist != null) {
            if (gist!!.createdAt() != null) {
                val stored = store.getGist(gist!!.id())
                if (stored == null) {
                    store.addGist(gist)
                }
            }
            gists = arrayOf(gist!!.id()!!)
        }

        val adapter = GistsPagerAdapter(this, gists)
        pagerHandler = PagerHandler(this, vp_pages, adapter)
        lifecycle.addObserver(pagerHandler!!)
        pagerHandler!!.onPagedChanged = this::onPageChanged
        vp_pages.scheduleSetItem(initialPosition, pagerHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(pagerHandler!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                return true
            }
            R.id.m_delete -> {
                val gistId = gists!![vp_pages.currentItem]
                val args = Bundle()
                args.putString(EXTRA_GIST_ID, gistId)
                ConfirmDialogFragment.show(this, REQUEST_CONFIRM_DELETE,
                    getString(R.string.confirm_gist_delete_title),
                    getString(R.string.confirm_gist_delete_message), args)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, arguments: Bundle) {
        if (REQUEST_CONFIRM_DELETE == requestCode && RESULT_OK == resultCode) {
            val gistId = arguments.getString(EXTRA_GIST_ID)

            ServiceGenerator.createService(this, GistService::class.java)
                .deleteGist(gistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxProgress.bindToLifecycle(this, R.string.deleting_gist))
                .`as`(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe({ response ->
                    setResult(RESULT_OK)
                    finish()
                }, { e ->
                    Log.d(TAG, "Exception deleting Gist", e)
                    ToastUtils.show(this, e.message)
                })
            return
        }

        pagerHandler!!.adapter
            .onDialogResult(vp_pages.currentItem, requestCode, resultCode, arguments)

        super.onDialogResult(requestCode, resultCode, arguments)
    }

    private fun onPageChanged(position: Int) {
        val gistId = gists!![position]
        val gist = store.getGist(gistId)
        updateActionBar(gist, gistId)
    }

    override fun startActivity(intent: Intent) {
        val converted = UriLauncherActivity.convert(intent)
        if (converted != null) {
            super.startActivity(converted)
        } else {
            super.startActivity(intent)
        }
    }

    private fun updateActionBar(gist: Gist?, gistId: String) {
        val actionBar = supportActionBar!!
        when {
            gist == null -> {
                actionBar.subtitle = null
                actionBar.setLogo(null)
                actionBar.setIcon(R.drawable.app_icon)
            }
            gist.owner() != null -> {
                avatars.bind(actionBar, gist.owner()!!)
                actionBar.subtitle = gist.owner()!!.login()
            }
            else -> {
                actionBar.setSubtitle(R.string.anonymous)
                actionBar.setLogo(null)
                actionBar.setIcon(R.drawable.app_icon)
            }
        }
        actionBar.title = getString(R.string.gist_title) + gistId
    }

    override fun loaded(gist: Gist) {
        if (gists!![vp_pages.currentItem] == gist.id()) {
            updateActionBar(gist, gist.id()!!)
        }
    }

    companion object {

        private val REQUEST_CONFIRM_DELETE = 1
        private val TAG = "GistsViewActivity"

        /**
         * Create an intent to show a single gist
         *
         * @param gist
         * @return intent
         */
        fun createIntent(gist: Gist): Intent {
            return Builder("gists.VIEW").gist(gist).add(EXTRA_POSITION, 0)
                .toIntent()
        }

        /**
         * Create an intent to show gists with an initial selected Gist
         *
         * @param items
         * @param position
         * @return intent
         */
        fun createIntent(items: List<Item<*>>, position: Int): Intent {
            val ids = arrayOfNulls<String>(items.size)
            for ((index, item) in items.withIndex()) {
                val gist = (item as GistItem).gist
                ids[index] = gist.id()
            }
            return Builder("gists.VIEW")
                .add(EXTRA_GIST_IDS, ids as Serializable)
                .add(EXTRA_POSITION, position).toIntent()
        }
    }
}
