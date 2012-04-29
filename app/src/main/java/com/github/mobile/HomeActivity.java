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
package com.github.mobile;

import static com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_LIST;
import static com.github.mobile.HomeDropdownListAdapter.ACTION_DASHBOARD;
import static com.github.mobile.HomeDropdownListAdapter.ACTION_FILTERS;
import static com.github.mobile.HomeDropdownListAdapter.ACTION_GISTS;
import static com.github.mobile.util.GitHubIntents.EXTRA_USER;
import static com.google.common.collect.Lists.newArrayList;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.gist.GistsActivity;
import com.github.mobile.issue.FilterBrowseActivity;
import com.github.mobile.issue.IssueDashboardActivity;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.repo.OrgLoader;
import com.github.mobile.repo.UserComparator;
import com.github.mobile.ui.user.UserPagerAdapter;
import com.github.mobile.util.AccountUtils;
import com.github.mobile.util.AvatarUtils;
import com.github.mobile.util.PreferenceUtils;
import com.github.mobile.util.GitHubIntents.Builder;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectView;

/**
 * Home screen activity
 */
public class HomeActivity extends RoboSherlockFragmentActivity implements OnNavigationListener,
        LoaderCallbacks<List<User>> {

    private static final String TAG = "GH.UVA";

    private static final String PREF_ORG_ID = "orgId";

    /**
     * Create intent for this activity
     *
     * @param org
     * @return intent
     */
    public static Intent createIntent(User org) {
        return new Builder("org.VIEW").add(EXTRA_USER, org).toIntent();
    }

    @Inject
    private AccountDataManager accountDataManager;

    @Inject
    private Provider<UserComparator> userComparatorProvider;

    private boolean isDefaultUser;

    private List<User> orgs = Collections.emptyList();

    private HomeDropdownListAdapter homeAdapter;

    private List<OrgSelectionListener> orgSelectionListeners = newArrayList();

    private User org;

    @InjectView(id.tpi_header)
    private TitlePageIndicator indicator;

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @Inject
    private AvatarUtils avatarHelper;

    @Inject
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager_with_title);

        getSupportLoaderManager().initLoader(0, null, this);

        User org = (User) getIntent().getSerializableExtra(EXTRA_USER);
        if (org == null && savedInstanceState != null)
            org = (User) savedInstanceState.getSerializable(EXTRA_USER);
        if (org != null)
            setOrg(org);
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

        homeAdapter = new HomeDropdownListAdapter(this, orgs, avatarHelper);
        actionBar.setListNavigationCallbacks(homeAdapter, this);
    }

    private void setOrg(User org) {
        Log.d(TAG, "setOrg : " + org.getLogin());

        PreferenceUtils.save(sharedPreferences.edit().putInt(PREF_ORG_ID, org.getId()));

        // Don't notify listeners or change pager if org hasn't changed
        if (this.org != null && this.org.getId() == org.getId())
            return;

        this.org = org;

        boolean isDefaultUser = isDefaultUser(org);
        PagerAdapter pagerAdater = pager.getAdapter();
        if (pagerAdater == null) {
            pager.setAdapter(new UserPagerAdapter(getSupportFragmentManager(), isDefaultUser));
            indicator.setViewPager(pager);
        } else if (this.isDefaultUser != isDefaultUser) {
            int item = pager.getCurrentItem();
            ((UserPagerAdapter) pagerAdater).clearAdapter();
            pager.setAdapter(new UserPagerAdapter(getSupportFragmentManager(), isDefaultUser(org)));
            indicator.setViewPager(pager);
            pager.setCurrentItem(item, false);
        }
        this.isDefaultUser = isDefaultUser;

        for (OrgSelectionListener listener : orgSelectionListeners)
            listener.onOrgSelected(org);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getSupportMenuInflater().inflate(menu.home, optionMenu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.search:
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
            case ACTION_FILTERS:
                startActivity(FilterBrowseActivity.createIntent());
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
        return new OrgLoader(this, accountDataManager, userComparatorProvider);
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

        for (int i = 0; i < orgs.size(); ++i) {
            User availableOrg = orgs.get(i);
            if (availableOrg.getId() == targetOrgId) {
                getSupportActionBar().setSelectedNavigationItem(i);
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<User>> listLoader) {
    }

    private boolean isDefaultUser(final User org) {
        final String accountLogin = AccountUtils.getLogin(this);
        return org != null && accountLogin != null && accountLogin.equals(org.getLogin());
    }

    public User registerOrgSelectionListener(OrgSelectionListener listener) {
        orgSelectionListeners.add(listener);
        return org;
    }

    public static interface OrgSelectionListener {
        public void onOrgSelected(User org);
    }

}
