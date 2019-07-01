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

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.base.FragmentPagerAdapter
import com.github.pockethub.android.ui.repo.RepositoryListFragment
import com.meisolsson.githubsdk.model.User

/**
 * Pager adapter for a user's different views
 *
 * @param fragment
 * @param defaultUser
 */
class HomePagerAdapter(
        fragment: Fragment,
        private val defaultUser: Boolean,
        private val org: User
) : FragmentPagerAdapter(fragment) {

    private val resources = fragment.resources

    override fun getItem(position: Int): Fragment? {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = if (defaultUser) UserReceivedNewsFragment() else OrganizationNewsFragment()
            1 -> fragment = RepositoryListFragment()
            2 -> fragment = if (defaultUser) MyFollowersFragment() else MembersFragment()
            3 -> fragment = MyFollowingFragment()
        }

        if (fragment != null) {
            val args = Bundle()
            args.putParcelable("org", org)
            fragment.arguments = args
        }
        return fragment
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position)
        return fragment
    }

    override fun getCount(): Int {
        return if (defaultUser) 4 else 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> resources.getString(R.string.tab_news)
            1 -> resources.getString(R.string.tab_repositories)
            2 -> resources.getString(
                    if (defaultUser) R.string.tab_followers_self else R.string.tab_members
            )
            3 -> resources.getString(R.string.tab_following_self)
            else -> null
        }
    }
}
