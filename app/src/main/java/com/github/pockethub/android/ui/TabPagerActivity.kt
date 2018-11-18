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
package com.github.pockethub.android.ui

import android.os.Bundle
import android.view.View
import android.widget.TabHost.OnTabChangeListener
import android.widget.TabHost.TabContentFactory
import androidx.annotation.Nullable
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.pager_with_tabs.*

/**
 * Activity with tabbed pages
 *
 * @param <V>
</V> */
abstract class TabPagerActivity<V> :
    PagerActivity(),
    OnTabChangeListener,
    TabContentFactory
    where V : PagerAdapter, V : FragmentProvider {

    /**
     * Pager adapter
     */
    protected var adapter: V? = null

    override fun onTabChanged(tabId: String) {}

    override fun createTabContent(tag: String): View {
        val view = View(application)
        view.visibility = View.GONE
        return view
    }

    /**
     * Create pager adapter
     *
     * @return pager adapter
     */
    protected abstract fun createAdapter(): V

    /**
     * Get title for position
     *
     * @param position
     * @return title
     */
    protected open fun getTitle(position: Int): String {
        return adapter!!.getPageTitle(position).toString()
    }

    /**
     * Get icon for position
     *
     * @param position
     * @return icon
     */
    protected open fun getIcon(position: Int): String? {
        return null
    }

    /**
     * Set tab and pager as gone or visible
     *
     * @param gone
     * @return this activity
     */
    protected fun setGone(gone: Boolean): TabPagerActivity<V> {
        if (gone) {
            sliding_tabs_layout.visibility = View.GONE
            vp_pages.visibility = View.GONE
        } else {
            sliding_tabs_layout.visibility = View.VISIBLE
            vp_pages.visibility = View.VISIBLE
        }
        return this
    }

    /**
     * Set current item to new position
     *
     *
     * This is guaranteed to only be called when a position changes and the
     * current item of the pager has already been updated to the given position
     *
     *
     * Sub-classes may override this method
     *
     * @param position
     */
    protected open fun setCurrentItem(position: Int) {
        // Intentionally left blank
    }

    private fun updateCurrentItem(newPosition: Int) {
        if (newPosition > -1 && newPosition < adapter!!.count) {
            vp_pages.setItem(newPosition)
            setCurrentItem(newPosition)
        }
    }

    private fun createPager() {
        adapter = createAdapter()
        invalidateOptionsMenu()
        vp_pages.adapter = adapter
    }

    private fun updateTabs() {
        sliding_tabs_layout.setupWithViewPager(vp_pages)
    }

    /**
     * Configure tabs and pager
     */
    protected fun configureTabPager() {
        if (adapter == null) {
            createPager()
            updateTabs()
        }
    }

    override fun onPostCreate(@Nullable savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        vp_pages.addOnPageChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        vp_pages.removeOnPageChangeListener(this)
    }

    override fun getProvider(): FragmentProvider? {
        return adapter
    }
}
