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

package com.github.pockethub.android.ui;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import com.github.pockethub.android.R;
import com.github.pockethub.android.accounts.AccountUtils;
import com.github.pockethub.android.accounts.AccountsHelper;
import com.github.pockethub.android.accounts.LoginActivity;
import com.github.pockethub.android.core.user.UserComparator;
import com.github.pockethub.android.persistence.AccountDataManager;
import com.github.pockethub.android.persistence.CacheHelper;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.ui.gist.GistsPagerFragment;
import com.github.pockethub.android.ui.issue.FilterListFragment;
import com.github.pockethub.android.ui.issue.IssueDashboardPagerFragment;
import com.github.pockethub.android.ui.notification.NotificationActivity;
import com.github.pockethub.android.ui.user.HomePagerFragment;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.ToastUtils;
import com.google.android.material.navigation.NavigationView;
import com.meisolsson.githubsdk.core.TokenStore;
import com.meisolsson.githubsdk.model.User;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    @BindView(R.id.drawer_layout)
    protected DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    protected NavigationView navigationView;

    @Inject
    protected AccountDataManager accountDataManager;

    @Inject
    protected Provider<UserComparator> userComparatorProvider;

    private List<User> orgs = Collections.emptyList();

    private User org;

    @Inject
    @Singleton
    protected AvatarLoader avatars;

    private boolean userLearnedDrawer;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @VisibleForTesting
    public Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, getToolbar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!userLearnedDrawer) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                    userLearnedDrawer = true;
                    Log.d(TAG, "User learned drawer");
                }
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        navigationView.setNavigationItemSelectedListener(this);

        reloadOrgs();

        TokenStore tokenStore = TokenStore.getInstance(this);

        if (tokenStore.getToken() == null) {
            AccountManager manager = AccountManager.get(this);
            Account[] accounts = manager.getAccountsByType(getString(R.string.account_type));
            if (accounts.length > 0) {
                Account account = accounts[0];
                AccountsHelper.getUserToken(this, account);
                tokenStore.saveToken(AccountsHelper.getUserToken(this, account));
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    private void reloadOrgs() {
        Single.fromCallable(() -> AccountUtils.getAccount(getAccountManager(), this))
                .map(account -> accountDataManager.getOrgs(false))
                .map(orgs -> {
                    Collections.sort(orgs, userComparatorProvider.get());
                    return orgs;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(this::onOrgsLoaded,
                        e -> {
                            Log.e(TAG, "Exception loading organizations", e);
                            ToastUtils.show(this, R.string.error_orgs_load);
                        });
    }

    @VisibleForTesting
    void onOrgsLoaded(List<User> orgs) {
        if (orgs.isEmpty()) {
            return;
        }

        org = orgs.get(0);
        this.orgs = orgs;

        setUpNavigationView();

        Window window = getWindow();
        if (window == null) {
            return;
        }
        View view = window.getDecorView();
        if (view == null) {
            return;
        }

        view.post(() -> {
            switchFragment(new HomePagerFragment(), org);
            if (!userLearnedDrawer) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getMenuInflater().inflate(R.menu.activity_main, optionMenu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = optionMenu.findItem(R.id.m_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(optionMenu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Restart loader if default account doesn't match currently loaded
        // account
        List<User> currentOrgs = orgs;
        if (currentOrgs != null && !currentOrgs.isEmpty()
                && !AccountUtils.isUser(this, currentOrgs.get(0))) {
            reloadOrgs();
        }
    }

    Map<MenuItem,User> menuItemOrganizationMap = new HashMap<>();

    private void setUpHeaderView() {
        ImageView userImage;
        TextView userRealName;
        TextView userName;

        View headerView = navigationView.getHeaderView(0);
        userImage = headerView.findViewById(R.id.user_picture);
        ImageView notificationIcon = headerView.findViewById(R.id.iv_notification);
        userRealName = headerView.findViewById(R.id.user_real_name);
        userName = headerView.findViewById(R.id.user_name);

        notificationIcon.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, NotificationActivity.class)));

        avatars.bind(userImage, org);
        userName.setText(org.login());

        String name = org.name();
        if (name != null) {
            userRealName.setText(org.name());
        } else {
            userRealName.setVisibility(View.GONE);
        }
    }

    private void setUpNavigationView() {
        setUpHeaderView();
        setUpNavigationMenu();
    }

    private void setUpNavigationMenu() {
        MenuItem organizationContainer = navigationView.getMenu().findItem(R.id.navigation_organizations);
        if (organizationContainer.hasSubMenu()) {
            SubMenu organizationsMenu = organizationContainer.getSubMenu();
            for (int i = 1; i < orgs.size(); i++) {
                User organization = orgs.get(i);
                if (organizationsMenu.findItem(organization.id().intValue()) == null) {
                    MenuItem organizationMenuItem = organizationsMenu.add(Menu.NONE, organization.id().intValue(), Menu.NONE, organization.name() != null ? organization.name() : organization.login());
                    organizationMenuItem.setIcon(R.drawable.ic_github_organization_black_24dp);
                    //Because of tinting the real image would became a grey block
                    //avatars.bind(organizationMenuItem, organization);
                    menuItemOrganizationMap.put(organizationMenuItem, organization);
                }
            }
        } else {
            throw new IllegalStateException("Menu item " + organizationContainer + " should have a submenu");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if (itemId == R.id.navigation_home) {
            switchFragment(new HomePagerFragment(), org);
            getSupportActionBar().setTitle(getString(R.string.app_name));
            return true;
        } else if (itemId == R.id.navigation_gists) {
            switchFragment(new GistsPagerFragment(), null);
            getSupportActionBar().setTitle(menuItem.getTitle());
            return true;
        } else if (itemId == R.id.navigation_issue_dashboard) {
            switchFragment(new IssueDashboardPagerFragment(), null);
            getSupportActionBar().setTitle(menuItem.getTitle());
            return true;
        } else if (itemId == R.id.navigation_bookmarks) {
            switchFragment(new FilterListFragment(), null);
            getSupportActionBar().setTitle(menuItem.getTitle());
            return true;
        } else if (itemId == R.id.navigation_log_out) {
            logout();
            return false;
        } else if (menuItemOrganizationMap.containsKey(menuItem)) {
            switchFragment(new HomePagerFragment(), menuItemOrganizationMap.get(menuItem));
            navigationView.getMenu().findItem(R.id.navigation_home).setChecked(true);
            return false;
        } else {
            throw new IllegalStateException("MenuItem " + menuItem + " not known");
        }
    }

    private void logout() {
        // Remove cookies so that the login is clean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
        } else {
            CookieManager.getInstance().removeAllCookie();
        }

        // Clear all of the cached data
        CacheHelper helper = new CacheHelper(this);
        helper.getWritableDatabase().delete("orgs", null, null);
        helper.getWritableDatabase().delete("users", null, null);
        helper.getWritableDatabase().delete("repos", null, null);

        // Remove the account
        AccountManager accountManager = getAccountManager();
        String accountType = getString(R.string.account_type);
        Account[] allGitHubAccounts = accountManager.getAccountsByType(accountType);

        for (Account account : allGitHubAccounts) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                accountManager.removeAccount(account, this, bool -> startLoginActivity(), null);
            } else {
                accountManager.removeAccount(account, bundle -> startLoginActivity(), null);
            }
        }
    }

    private void startLoginActivity() {
        Intent in = new Intent(this, LoginActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        finish();
    }

    private void switchFragment(Fragment fragment, User organization) {
        if (organization != null) {
            Bundle args = new Bundle();
            args.putParcelable("org", organization);
            fragment.setArguments(args);
        }
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, fragment).commit();
        drawerLayout.closeDrawer(GravityCompat.START);

        currentFragment = fragment;
    }

    private AccountManager getAccountManager() {
        return AccountManager.get(this);
    }
}
