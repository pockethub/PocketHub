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
package com.github.pockethub.android.ui.search

import android.app.SearchManager
import android.app.SearchManager.QUERY
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEARCH
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.ui.MainActivity
import com.github.pockethub.android.ui.PagerHandler
import com.github.pockethub.android.util.ToastUtils
import kotlinx.android.synthetic.main.pager_with_tabs.*
import kotlinx.android.synthetic.main.tabbed_progress_pager.*

/**
 * Activity to view search results
 */
class SearchActivity : BaseActivity() {

    private var repoFragment: SearchRepositoryListFragment? = null

    private var userFragment: SearchUserListFragment? = null

    private var searchView: SearchView? = null

    private var lastQuery: String? = null

    private var pagerHandler: PagerHandler<SearchPagerAdapter>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabbed_progress_pager)

        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)

        configurePager()
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_search, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.m_search)
        searchView = searchItem.actionView as SearchView
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_search -> {
                searchView!!.post { searchView!!.setQuery(lastQuery, false) }
                return true
            }
            R.id.m_clear -> {
                RepositorySearchSuggestionsProvider.clear(this)
                ToastUtils.show(this, R.string.search_history_cleared)
                return true
            }
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (ACTION_SEARCH == intent.action) {
            search(intent.getStringExtra(QUERY))
        }
    }

    private fun search(query: String) {
        lastQuery = query
        supportActionBar!!.title = query
        RepositorySearchSuggestionsProvider.save(this, query)

        if (repoFragment == null || userFragment == null) {
            findFragments()
        }

        if (repoFragment != null && userFragment != null) {
            repoFragment!!.pagedListFetcher.refresh()
            userFragment!!.pagedListFetcher.refresh()
        }
    }

    private fun configurePager() {
        val adapter = SearchPagerAdapter(this)
        pagerHandler = PagerHandler(this, vp_pages, adapter)
        lifecycle.addObserver(pagerHandler!!)
        pagerHandler!!.tabs = sliding_tabs_layout

        pb_loading.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(pagerHandler!!)
    }

    private fun findFragments() {
        if (repoFragment == null || userFragment == null) {
            val fm = supportFragmentManager
            repoFragment = fm.findFragmentByTag(
                "android:switcher:${vp_pages.id}:0") as SearchRepositoryListFragment
            userFragment = fm.findFragmentByTag(
                "android:switcher:${vp_pages.id}:1") as SearchUserListFragment
        }
    }
}
