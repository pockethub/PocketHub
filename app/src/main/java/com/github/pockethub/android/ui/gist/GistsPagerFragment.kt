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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.GIST_VIEW
import com.github.pockethub.android.core.gist.GistStore
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.rx.RxProgress
import com.github.pockethub.android.ui.PagerHandler
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Gist
import com.meisolsson.githubsdk.service.gists.GistService
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.pager_with_tabs.*
import kotlinx.android.synthetic.main.pager_with_tabs.view.*
import java.util.Random
import javax.inject.Inject

class GistsPagerFragment : BaseFragment() {

    @Inject
    lateinit var store: GistStore

    private val rand: Random = Random()

    private var pagerHandler: PagerHandler<GistQueriesPagerAdapter>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pager_with_tabs, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        view.toolbar.visibility = View.GONE
        configurePager()
    }

    private fun configurePager() {
        val adapter = GistQueriesPagerAdapter(this)
        pagerHandler = PagerHandler(this, vp_pages, adapter)
        lifecycle.addObserver(pagerHandler!!)
        pagerHandler!!.tabs = sliding_tabs_layout
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(pagerHandler!!)
    }

    private fun randomGist() {
        val service = ServiceGenerator.createService(activity, GistService::class.java)

        service.getPublicGists(1)
            .flatMap { response ->
                val firstPage = response.body()
                var randomPage = (Math.random() * (firstPage.last()!! - 1)).toInt()
                randomPage = Math.max(1, randomPage)

                return@flatMap service.getPublicGists(randomPage.toLong())
            }
            .flatMap { response ->
                val gistPage = response.body()
                if (gistPage.items().isEmpty()) {
                    var randomPage = (Math.random() * (gistPage.last()!! - 1)).toInt()
                    randomPage = Math.max(1, randomPage)
                    return@flatMap service.getPublicGists(randomPage.toLong())
                }

                return@flatMap Single.just(response)
            }
            .map<Gist> { response ->
                val gistPage = response.body()
                if (response.isSuccessful) {
                    val size = gistPage.items().size
                    if (size > 0) {
                        return@map store.addGist(gistPage.items()[rand.nextInt(size)])
                    } else {
                        throw IllegalArgumentException(context!!.getString(R.string.no_gists_found))
                    }
                } else {
                    ToastUtils.show(activity, R.string.error_gist_load)
                    return@map null
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(RxProgress.bindToLifecycle(activity, R.string.random_gist))
            .`as`(AutoDisposeUtils.bindToLifecycle<Gist>(this))
            .subscribe({ gist ->
                activity!!.startActivityForResult(GistsViewActivity.createIntent(gist), GIST_VIEW)
            }, { e ->
                Log.d(TAG, "Exception opening random Gist", e)
                ToastUtils.show(activity, e.message)
            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_gists, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.m_random -> {
                randomGist()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private val TAG = "GistsPagerFragment"
    }
}
