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

package com.github.pockethub.android.ui.user

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import com.github.pockethub.android.R
import com.github.pockethub.android.accounts.AccountUtils
import com.github.pockethub.android.ui.PagerHandler
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.util.PreferenceUtils
import com.meisolsson.githubsdk.model.User
import kotlinx.android.synthetic.main.pager_with_tabs.*
import kotlinx.android.synthetic.main.pager_with_tabs.view.*

class HomePagerFragment : BaseFragment() {

    private var sharedPreferences: SharedPreferences? = null

    private var isDefaultUser: Boolean = false

    private var org: User? = null

    private var pagerHandler: PagerHandler<HomePagerAdapter>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pager_with_tabs, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.toolbar.visibility = View.GONE
        sharedPreferences = activity!!.getPreferences(Context.MODE_PRIVATE)
        setOrg(arguments!!.getParcelable("org")!!)
    }

    private fun setOrg(org: User) {
        PreferenceUtils.save(sharedPreferences!!.edit().putInt(PREF_ORG_ID, org.id()!!.toInt()))
        this.org = org
        this.isDefaultUser = AccountUtils.isUser(activity, org)
        configurePager()
    }

    private fun configurePager() {
        val adapter = HomePagerAdapter(this, isDefaultUser, org)
        pagerHandler = PagerHandler(this, vp_pages, adapter)
        lifecycle.addObserver(pagerHandler!!)
        pagerHandler!!.tabs = sliding_tabs_layout
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(pagerHandler!!)
    }

    companion object {

        private val TAG = "HomePagerFragment"

        private val PREF_ORG_ID = "orgId"
    }
}
