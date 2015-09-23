package com.github.pockethub.ui;


import static com.github.pockethub.ui.NavigationDrawerObject.TYPE_SEPERATOR;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.alorma.github.basesdk.client.StoreCredentials;
import com.alorma.github.sdk.bean.dto.response.Organization;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.login.AccountsHelper;
import com.bugsnag.android.Bugsnag;
import com.github.pockethub.R;
import com.github.pockethub.accounts.AccountUtils;
import com.github.pockethub.accounts.LoginActivity;
import com.github.pockethub.core.user.UserComparator;
import com.github.pockethub.persistence.AccountDataManager;
import com.github.pockethub.ui.gist.GistsPagerFragment;
import com.github.pockethub.ui.issue.FilterListFragment;
import com.github.pockethub.ui.issue.IssueDashboardPagerFragment;
import com.github.pockethub.ui.repo.OrganizationLoader;
import com.github.pockethub.ui.user.HomePagerFragment;
import com.github.pockethub.util.AvatarLoader;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Collections;
import java.util.List;

import static com.github.pockethub.ui.NavigationDrawerObject.TYPE_SEPERATOR;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
    LoaderManager.LoaderCallbacks<List<Organization>> {

    private static final String TAG = "MainActivity";

    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Inject
    private AccountDataManager accountDataManager;

    @Inject
    private Provider<UserComparator> userComparatorProvider;

    private List<Organization> orgs = Collections.emptyList();

    private NavigationDrawerAdapter navigationAdapter;

    private Organization org;

    @Inject
    private AvatarLoader avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bugsnag.init(this);
        setContentView(R.layout.activity_main);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        getSupportLoaderManager().initLoader(0, null, this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
            getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        StoreCredentials storeCredentials = new StoreCredentials(this);

        if(storeCredentials.token() == null){
            AccountManager manager = AccountManager.get(this);
            Account[] accounts = manager.getAccountsByType(getString(R.string.account_type));
            if(accounts.length > 0) {
                Account account = accounts[0];
                AccountsHelper.getUserToken(this, account);
                storeCredentials.storeToken(AccountsHelper.getUserToken(this, account));
                storeCredentials.storeUsername(account.name);
                storeCredentials.storeScopes(AccountsHelper.getUserScopes(this, account));
            }
        }
    }

    private void reloadOrgs() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getMenuInflater().inflate(R.menu.home, optionMenu);

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
        List<Organization> currentOrgs = orgs;
        if (currentOrgs != null && !currentOrgs.isEmpty()
            && !AccountUtils.isUser(this, currentOrgs.get(0)))
            reloadOrgs();
    }

    @Override
    public Loader<List<Organization>> onCreateLoader(int i, Bundle bundle) {
        return new OrganizationLoader(this, accountDataManager,
            userComparatorProvider);
    }

    @Override
    public void onLoadFinished(Loader<List<Organization>> listLoader, final List<Organization> orgs) {
        if (orgs.isEmpty())
            return;

        org = orgs.get(0);
        this.orgs = orgs;

        if (navigationAdapter != null)
            navigationAdapter.setOrgs(orgs);
        else {
            navigationAdapter = new NavigationDrawerAdapter(MainActivity.this, orgs, avatars);
            mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), navigationAdapter, avatars, org);

            Window window = getWindow();
            if (window == null)
                return;
            View view = window.getDecorView();
            if (view == null)
                return;

            view.post(new Runnable() {

                @Override
                public void run() {
                    MainActivity.this.onNavigationDrawerItemSelected(1);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Organization>> listLoader) {

    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (navigationAdapter.getItem(position).getType() == TYPE_SEPERATOR)
            return;
        Fragment fragment;
        Bundle args = new Bundle();
        switch (position) {
            case 1:
                fragment = new HomePagerFragment();
                args.putParcelable("org", org);
                break;
            case 2:
                fragment = new GistsPagerFragment();
                break;
            case 3:
                fragment = new IssueDashboardPagerFragment();
                break;
            case 4:
                fragment = new FilterListFragment();
                break;
            case 5:
                Account[] allAccounts = AccountManager.get(this).getAccounts();

                for (Account account : allAccounts) {
                    AccountManager.get(this).removeAccount(account, null, null);
                }

                Intent in = new Intent(this, LoginActivity.class);
                in.addFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);
                finish();
                return;
            default:
                fragment = new HomePagerFragment();
                args.putParcelable("org", orgs.get(position - 6));
                break;
        }
        fragment.setArguments(args);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, fragment).commit();
    }

}
