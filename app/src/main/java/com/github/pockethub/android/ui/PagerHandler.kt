package com.github.pockethub.android.ui

import android.app.Activity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.pockethub.android.ui.base.BaseFragment
import com.google.android.material.tabs.TabLayout

class PagerHandler<V> private constructor(
    private val pager: ViewPager,
    val adapter: V,
    private val activity: Activity
): LifecycleObserver,
    ViewPager.OnPageChangeListener,
    OptionsMenuListener
    where V : FragmentProvider, V : PagerAdapter {

    var onPagedChanged: (Int) -> Unit = {}

    var tabs: TabLayout? = null
        set(value) {
            value?.setupWithViewPager(pager)
        }

    constructor(
        activity: BaseActivity,
        pager: ViewPager,
        adapter: V
    ): this(pager, adapter, activity) {
        activity.optionsMenuListener = this
    }

    constructor(
        fragment: BaseFragment,
        pager: ViewPager,
        adapter: V
    ): this(pager, adapter, fragment.activity!!) {
        fragment.optionsMenuListener = this
    }

    init {
        pager.adapter = adapter
        pager.addOnPageChangeListener(this)
        activity.invalidateOptionsMenu()
    }

    /**
     * Set tab and pager as gone or visible
     *
     * @param gone
     * @return this activity
     */
    fun setGone(gone: Boolean) {
        if (gone) {
            tabs?.visibility = View.GONE
            pager.visibility = View.GONE
        } else {
            tabs?.visibility = View.VISIBLE
            pager.visibility = View.VISIBLE
        }
    }

    /**
     * Get selected fragment
     *
     * @return fragment
     */
    private fun getFragment(): Fragment? {
        return adapter.selected
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?): Boolean {
        val fragment = getFragment()
        if (fragment !== null) {
            fragment.onCreateOptionsMenu(menu, inflater)
            return true
        }

        return false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val fragment = getFragment()
        if (fragment !== null) {
            return fragment.onOptionsItemSelected(item)
        }

        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val fragment = getFragment()
        if (fragment !== null) {
            fragment.onPrepareOptionsMenu(menu)
            return true
        }

        return false
    }

    override fun invalidateOptionsMenu() {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        activity.invalidateOptionsMenu()
        onPagedChanged(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }
}