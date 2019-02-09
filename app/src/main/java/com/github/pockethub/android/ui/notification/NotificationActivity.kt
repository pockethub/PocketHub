package com.github.pockethub.android.ui.notification

import android.os.Bundle
import android.view.MenuItem
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.ui.PagerHandler
import kotlinx.android.synthetic.main.pager_with_tabs.*

class NotificationActivity : BaseActivity() {

    private var pagerHandler: PagerHandler<NotificationPagerAdapter>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pager_with_tabs)

        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)

        configurePager()
    }

    fun configurePager() {
        val adapter = NotificationPagerAdapter(this)
        pagerHandler = PagerHandler(this, vp_pages, adapter)
        lifecycle.addObserver(pagerHandler!!)
        pagerHandler!!.tabs = sliding_tabs_layout
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(pagerHandler!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
