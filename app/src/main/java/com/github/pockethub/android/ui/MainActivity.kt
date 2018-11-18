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

import android.accounts.AccountManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.github.pockethub.android.R
import com.github.pockethub.android.accounts.AccountUtils
import com.github.pockethub.android.accounts.AccountsHelper
import com.github.pockethub.android.accounts.LoginActivity
import com.github.pockethub.android.core.user.UserComparator
import com.github.pockethub.android.persistence.AccountDataManager
import com.github.pockethub.android.persistence.CacheHelper
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.ui.gist.GistsPagerFragment
import com.github.pockethub.android.ui.issue.FilterListFragment
import com.github.pockethub.android.ui.issue.IssueDashboardPagerFragment
import com.github.pockethub.android.ui.notification.NotificationActivity
import com.github.pockethub.android.ui.user.HomePagerFragment
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ToastUtils
import com.google.android.material.navigation.NavigationView
import com.meisolsson.githubsdk.core.TokenStore
import com.meisolsson.githubsdk.model.User
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Collections
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

// TODO: Remove ButterKnife in favour of Kotlin's view handling
// TODO: Move pager logic to composition instead of inheritance
// TODO: Have another think about MVI
// TODO: Figure out good way to inject github-sdk services dynamically with Dagger. If possible
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var accountDataManager: AccountDataManager

    @Inject
    lateinit var userComparatorProvider: Provider<UserComparator>

    @Inject
    @Singleton
    lateinit var avatars: AvatarLoader

    private var orgs: List<User> = emptyList()

    private var org: User? = null

    private var userLearnedDrawer: Boolean = false
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    @VisibleForTesting
    var currentFragment: Fragment? = null

    private var menuItemOrganizationMap: MutableMap<MenuItem, User> = HashMap()

    private val accountManager: AccountManager
        get() = AccountManager.get(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        actionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)

                if (!userLearnedDrawer) {
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply()
                    userLearnedDrawer = true
                    Log.d(TAG, "User learned drawer")
                }
            }
        }

        drawer_layout.addDrawerListener(actionBarDrawerToggle)
        navigation_view.setNavigationItemSelectedListener(this)

        reloadOrgs()

        val tokenStore = TokenStore.getInstance(this)

        if (tokenStore.token == null) {
            val manager = AccountManager.get(this)
            val accounts = manager.getAccountsByType(getString(R.string.account_type))
            if (accounts.isNotEmpty()) {
                val account = accounts[0]
                AccountsHelper.getUserToken(this, account)
                tokenStore.saveToken(AccountsHelper.getUserToken(this, account))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        drawer_layout.removeDrawerListener(actionBarDrawerToggle)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    private fun reloadOrgs() {
        Single.fromCallable { AccountUtils.getAccount(accountManager, this) }
            .map { account -> accountDataManager.getOrgs(false) }
            .map { orgs ->
                Collections.sort(orgs, userComparatorProvider.get())
                orgs
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDisposeUtils.bindToLifecycle(this))
            .subscribe({ this.onOrgsLoaded(it) },
                { e ->
                    Log.e(TAG, "Exception loading organizations", e)
                    ToastUtils.show(this, R.string.error_orgs_load)
                })
    }

    @VisibleForTesting
    fun onOrgsLoaded(orgs: List<User>) {
        if (orgs.isEmpty()) {
            return
        }

        org = orgs[0]
        this.orgs = orgs

        setUpNavigationView()

        val window = window ?: return
        val view = window.decorView ?: return

        view.post {
            switchFragment(HomePagerFragment(), org)
            if (!userLearnedDrawer) {
                drawer_layout.openDrawer(GravityCompat.START)
            }
        }
    }

    override fun onCreateOptionsMenu(optionMenu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, optionMenu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = optionMenu.findItem(R.id.m_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        return super.onCreateOptionsMenu(optionMenu)
    }

    override fun onResume() {
        super.onResume()

        // Restart loader if default account doesn't match currently loaded
        // account
        val currentOrgs = orgs
        if (currentOrgs.isNotEmpty()
            && !AccountUtils.isUser(this, currentOrgs[0])) {
            reloadOrgs()
        }
    }

    private fun setUpHeaderView() {
        val userImage: ImageView
        val userRealName: TextView
        val userName: TextView

        val headerView = navigation_view.getHeaderView(0)
        userImage = headerView.findViewById(R.id.user_picture)
        val notificationIcon = headerView.findViewById<ImageView>(R.id.iv_notification)
        userRealName = headerView.findViewById(R.id.user_real_name)
        userName = headerView.findViewById(R.id.user_name)

        notificationIcon.setOnClickListener { v ->
            startActivity(Intent(this@MainActivity, NotificationActivity::class.java))
        }

        avatars.bind(userImage, org)
        userName.text = org!!.login()

        val name = org!!.name()
        if (name != null) {
            userRealName.text = org!!.name()
        } else {
            userRealName.visibility = View.GONE
        }
    }

    private fun setUpNavigationView() {
        setUpHeaderView()
        setUpNavigationMenu()
    }

    private fun setUpNavigationMenu() {
        val organizationContainer = navigation_view.menu.findItem(R.id.navigation_organizations)
        if (organizationContainer.hasSubMenu()) {
            val organizationsMenu = organizationContainer.subMenu
            for (i in 1 until orgs.size) {
                val organization = orgs[i]
                if (organizationsMenu.findItem(organization.id()!!.toInt()) == null) {
                    val title = organization.name() ?: organization.login()
                    val organizationMenuItem = organizationsMenu.add(
                        Menu.NONE,
                        organization.id()!!.toInt(),
                        Menu.NONE,
                        title
                    )

                    organizationMenuItem.setIcon(R.drawable.ic_github_organization_black_24dp)
                    //Because of tinting the real image would became a grey block
                    //avatars.bind(organizationMenuItem, organization);
                    menuItemOrganizationMap[organizationMenuItem] = organization
                }
            }
        } else {
            throw IllegalStateException("Menu item $organizationContainer should have a submenu")
        }
    }

    override fun onNavigationItemSelected(@NonNull menuItem: MenuItem): Boolean {
        val itemId = menuItem.itemId

        if (itemId == R.id.navigation_home) {
            switchFragment(HomePagerFragment(), org)
            supportActionBar!!.setTitle(R.string.app_name)
            return true
        } else if (itemId == R.id.navigation_gists) {
            switchFragment(GistsPagerFragment(), null)
            supportActionBar!!.title = menuItem.title
            return true
        } else if (itemId == R.id.navigation_issue_dashboard) {
            switchFragment(IssueDashboardPagerFragment(), null)
            supportActionBar!!.title = menuItem.title
            return true
        } else if (itemId == R.id.navigation_bookmarks) {
            switchFragment(FilterListFragment(), null)
            supportActionBar!!.title = menuItem.title
            return true
        } else if (itemId == R.id.navigation_log_out) {
            logout()
            return false
        } else if (menuItemOrganizationMap.containsKey(menuItem)) {
            switchFragment(HomePagerFragment(), menuItemOrganizationMap[menuItem])
            navigation_view.menu.findItem(R.id.navigation_home).isChecked = true
            return false
        } else {
            throw IllegalStateException("MenuItem $menuItem not known")
        }
    }

    private fun logout() {
        // Remove cookies so that the login is clean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null)
        } else {
            CookieManager.getInstance().removeAllCookie()
        }

        // Clear all of the cached data
        val helper = CacheHelper(this)
        helper.writableDatabase.delete("orgs", null, null)
        helper.writableDatabase.delete("users", null, null)
        helper.writableDatabase.delete("repos", null, null)

        // Remove the account
        val accountManager = accountManager
        val accountType = getString(R.string.account_type)
        val allGitHubAccounts = accountManager.getAccountsByType(accountType)

        for (account in allGitHubAccounts) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                accountManager.removeAccount(account, this, { bool -> startLoginActivity() }, null)
            } else {
                accountManager.removeAccount(account, { bundle -> startLoginActivity() }, null)
            }
        }
    }

    private fun startLoginActivity() {
        val `in` = Intent(this, LoginActivity::class.java)
        `in`.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(`in`)
        finish()
    }

    private fun switchFragment(fragment: Fragment, organization: User?) {
        if (organization != null) {
            val args = Bundle()
            args.putParcelable("org", organization)
            fragment.arguments = args
        }
        val manager = supportFragmentManager
        manager.beginTransaction().replace(R.id.container, fragment).commit()
        drawer_layout.closeDrawer(GravityCompat.START)

        currentFragment = fragment
    }

    companion object {

        private val TAG = "MainActivity"
        private val PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned"
    }
}
