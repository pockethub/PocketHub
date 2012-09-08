/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.user;

import static com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_LIST;
import static com.github.mobile.Intents.EXTRA_USER;
import static com.github.mobile.ui.user.HomeDropdownListAdapter.ACTION_BOOKMARKS;
import static com.github.mobile.ui.user.HomeDropdownListAdapter.ACTION_DASHBOARD;
import static com.github.mobile.ui.user.HomeDropdownListAdapter.ACTION_GISTS;
import static com.github.mobile.util.TypefaceUtils.ICON_FOLLOW;
import static com.github.mobile.util.TypefaceUtils.ICON_NEWS;
import static com.github.mobile.util.TypefaceUtils.ICON_PUBLIC;
import static com.github.mobile.util.TypefaceUtils.ICON_TEAM;
import static com.github.mobile.util.TypefaceUtils.ICON_WATCH;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.id;
import com.github.mobile.R.menu;
import com.github.mobile.accounts.AccountUtils;
import com.github.mobile.core.user.UserComparator;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.ui.TabPagerActivity;
import com.github.mobile.ui.gist.GistsActivity;
import com.github.mobile.ui.issue.FiltersViewActivity;
import com.github.mobile.ui.issue.IssueDashboardActivity;
import com.github.mobile.ui.repo.OrganizationLoader;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.PreferenceUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.egit.github.core.User;

/**
 * Home screen activity
 */
public class HomeActivity extends TabPagerActivity<HomePagerAdapter> implements
        OnNavigationListener, OrganizationSelectionProvider,
        LoaderCallbacks<List<User>> {

    private static final String TAG = "HomeActivity";

    private static final String PREF_ORG_ID = "orgId";

    @Inject
    private AccountDataManager accountDataManager;

    @Inject
    private Provider<UserComparator> userComparatorProvider;

    private boolean isDefaultUser;

    private List<User> orgs = Collections.emptyList();

    private HomeDropdownListAdapter homeAdapter;

    private Set<OrganizationSelectionListener> orgSelectionListeners = new LinkedHashSet<OrganizationSelectionListener>();

    private User org;

    @Inject
    private AvatarLoader avatars;

    @Inject
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportLoaderManager().initLoader(0, null, this);

        User org = (User) getIntent().getSerializableExtra(EXTRA_USER);
        if (org == null && savedInstanceState != null)
            org = (User) savedInstanceState.getSerializable(EXTRA_USER);
        if (org != null) {
            isDefaultUser = AccountUtils.isUser(this, org);
            setOrg(org);
        }
    }

    private void reloadOrgs() {
        getSupportLoaderManager().restartLoader(0, null,
                new LoaderCallbacks<List<User>>() {

                    @Override
                    public Loader<List<User>> onCreateLoader(int id,
                            Bundle bundle) {
                        return HomeActivity.this.onCreateLoader(id, bundle);
                    }

                    @Override
                    public void onLoadFinished(Loader<List<User>> loader,
                            final List<User> users) {
                        HomeActivity.this.onLoadFinished(loader, users);
                        if (users.isEmpty())
                            return;

                        Window window = getWindow();
                        if (window == null)
                            return;
                        View view = window.getDecorView();
                        if (view == null)
                            return;

                        view.post(new Runnable() {

                            @Override
                            public void run() {
                                isDefaultUser = false;
                                setOrg(users.get(0));
                            }
                        });
                    }

                    @Override
                    public void onLoaderReset(Loader<List<User>> loader) {
                        HomeActivity.this.onLoaderReset(loader);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Restart loader if default account doesn't match currently loaded
        // account
        List<User> currentOrgs = orgs;
        if (currentOrgs != null && !currentOrgs.isEmpty()
                && !AccountUtils.isUser(this, currentOrgs.get(0)))
            reloadOrgs();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (org != null)
            outState.putSerializable(EXTRA_USER, org);
    }

    private void configureActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(NAVIGATION_MODE_LIST);

        homeAdapter = new HomeDropdownListAdapter(this, orgs, avatars);
        actionBar.setListNavigationCallbacks(homeAdapter, this);
    }

    private void setOrg(User org) {
        Log.d(TAG, "setOrg : " + org.getLogin());

        PreferenceUtils.save(sharedPreferences.edit().putInt(PREF_ORG_ID,
                org.getId()));

        // Don't notify listeners or change pager if org hasn't changed
        if (this.org != null && this.org.getId() == org.getId())
            return;

        this.org = org;

        boolean isDefaultUser = AccountUtils.isUser(this, org);
        boolean changed = this.isDefaultUser != isDefaultUser;
        this.isDefaultUser = isDefaultUser;
        if (adapter == null)
            configureTabPager();
        else if (changed) {
            int item = pager.getCurrentItem();
            adapter.clearAdapter(isDefaultUser);
            adapter.notifyDataSetChanged();
            createTabs();
            if (item >= adapter.getCount())
                item = adapter.getCount() - 1;
            pager.setItem(item);
        }

        for (OrganizationSelectionListener listener : orgSelectionListeners)
            listener.onOrganizationSelected(org);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getSupportMenuInflater().inflate(menu.home, optionMenu);

        return super.onCreateOptionsMenu(optionMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.m_search:
            onSearchRequested();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (homeAdapter.isOrgPosition(itemPosition)) {
            homeAdapter.setSelected(itemPosition);
            setOrg(orgs.get(itemPosition));
        } else {
            switch (homeAdapter.getAction(itemPosition)) {
            case ACTION_GISTS:
                startActivity(new Intent(this, GistsActivity.class));
                break;
            case ACTION_DASHBOARD:
                startActivity(new Intent(this, IssueDashboardActivity.class));
                break;
            case ACTION_BOOKMARKS:
                startActivity(FiltersViewActivity.createIntent());
                break;
            }
            int orgSelected = homeAdapter.getSelected();
            ActionBar actionBar = getSupportActionBar();
            if (orgSelected < actionBar.getNavigationItemCount())
                actionBar.setSelectedNavigationItem(orgSelected);
        }
        return true;
    }

    @Override
    public Loader<List<User>> onCreateLoader(int i, Bundle bundle) {
        return new OrganizationLoader(this, accountDataManager,
                userComparatorProvider);
    }

    @Override
    public void onLoadFinished(Loader<List<User>> listLoader, List<User> orgs) {
        this.orgs = orgs;

        if (homeAdapter != null)
            homeAdapter.setOrgs(orgs);
        else
            configureActionBar();

        int sharedPreferencesOrgId = sharedPreferences.getInt(PREF_ORG_ID, -1);
        int targetOrgId = org == null ? sharedPreferencesOrgId : org.getId();

        ActionBar actionBar = getSupportActionBar();
        for (int i = 0; i < orgs.size(); i++)
            if (orgs.get(i).getId() == targetOrgId) {
                actionBar.setSelectedNavigationItem(i);
                break;
            }
    }

    @Override
    public void onLoaderReset(Loader<List<User>> listLoader) {
    }

    @Override
    public User addListener(OrganizationSelectionListener listener) {
        if (listener != null)
            orgSelectionListeners.add(listener);
        return org;
    }

    @Override
    public OrganizationSelectionProvider removeListener(
            OrganizationSelectionListener listener) {
        if (listener != null)
            orgSelectionListeners.remove(listener);
        return this;
    }

    @Override
    protected HomePagerAdapter createAdapter() {
        return new HomePagerAdapter(this, isDefaultUser);
    }

    @Override
    protected String getIcon(int position) {
        switch (position) {
        case 0:
            return ICON_NEWS;
        case 1:
            return ICON_PUBLIC;
        case 2:
            return isDefaultUser ? ICON_WATCH : ICON_TEAM;
        case 3:
            return ICON_FOLLOW;
        default:
            return super.getIcon(position);
        }
    }
}
