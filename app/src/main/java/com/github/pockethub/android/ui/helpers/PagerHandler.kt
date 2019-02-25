package com.github.pockethub.android.ui.helpers

import android.app.Activity
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.pockethub.android.ui.FragmentProvider
import com.github.pockethub.android.ui.base.BaseActivity
import com.github.pockethub.android.ui.base.BaseFragment
import com.google.android.material.tabs.TabLayout

class PagerHandler<V> private constructor(
    private val pager: ViewPager,
    val adapter: V,
    private val activity: Activity
): LifecycleObserver,
    ViewPager.OnPageChangeListener
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
    ): this(pager, adapter, activity)

    constructor(
        fragment: BaseFragment,
        pager: ViewPager,
        adapter: V
    ): this(pager, adapter, fragment.activity!!)

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

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        activity.invalidateOptionsMenu()
        onPagedChanged(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }
}