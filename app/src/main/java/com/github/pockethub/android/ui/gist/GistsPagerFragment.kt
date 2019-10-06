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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.helpers.PagerHandler
import kotlinx.android.synthetic.main.pager_with_tabs.*
import kotlinx.android.synthetic.main.pager_with_tabs.view.*

class GistsPagerFragment : BaseFragment() {

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_gists, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {

        private val TAG = "GistsPagerFragment"
    }
}
