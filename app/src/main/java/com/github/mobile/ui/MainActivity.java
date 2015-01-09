package com.github.mobile.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;

import com.github.mobile.R;
import com.github.mobile.accounts.AccountUtils;
import com.github.mobile.core.user.UserComparator;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.ui.gist.GistsPagerFragment;
import com.github.mobile.ui.issue.FiltersViewFragment;
import com.github.mobile.ui.issue.IssueDashboardPagerFragment;
import com.github.mobile.ui.repo.OrganizationLoader;
import com.github.mobile.ui.user.HomeActivity;
import com.github.mobile.ui.user.HomePagerFragment;
import com.github.mobile.ui.user.OrganizationNewsFragment;
import com.github.mobile.ui.user.OrganizationSelectionListener;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.egit.github.core.User;

/**
 * Created by Henrik on 2015-01-08.
 */
public class MainActivity extends BaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
    LoaderManager.LoaderCallbacks<List<User>> {

    private static final String TAG = "MainActivity";

    private static final String PREF_ORG_ID = "orgId";

    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Inject
    private AccountDataManager accountDataManager;

    @Inject
    private Provider<UserComparator> userComparatorProvider;

    private boolean isDefaultUser;

    private List<User> orgs = Collections.emptyList();

    private NavigationDrawerAdapter navigationAdapter;

    private Set<OrganizationSelectionListener> orgSelectionListeners = new LinkedHashSet<OrganizationSelectionListener>();

    private User org;

    @Inject
    private AvatarLoader avatars;

    @Inject
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportLoaderManager().initLoader(0, null, this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
            getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    }

    private void reloadOrgs() {
        getSupportLoaderManager().restartLoader(0, null,
            new LoaderManager.LoaderCallbacks<List<User>>() {

                @Override
                public Loader<List<User>> onCreateLoader(int id,
                    Bundle bundle) {
                    return MainActivity.this.onCreateLoader(id, bundle);
                }

                @Override
                public void onLoadFinished(Loader<List<User>> loader,
                    final List<User> users) {
                    MainActivity.this.onLoadFinished(loader, users);
                }

                @Override
                public void onLoaderReset(Loader<List<User>> loader) {
                    MainActivity.this.onLoaderReset(loader);
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
    public Loader<List<User>> onCreateLoader(int i, Bundle bundle) {
        return new OrganizationLoader(this, accountDataManager,
            userComparatorProvider);
    }

    @Override
    public void onLoadFinished(Loader<List<User>> listLoader, final List<User> orgs) {
        org = orgs.get(0);
        this.orgs = orgs;

        if (navigationAdapter != null)
            navigationAdapter.setOrgs(orgs);
        else {
            navigationAdapter = new NavigationDrawerAdapter(MainActivity.this, orgs, avatars);
            mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), navigationAdapter, avatars, org);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<User>> listLoader) {

    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if(((NavigationDrawerObject)navigationAdapter.getItem(position)).getType() == NavigationDrawerObject.TYPE_SEPERATOR)
            return;
        Fragment fragmet = null;
        Bundle args = new Bundle();
        switch (position) {
            case 0:
                fragmet = new HomePagerFragment();
                args.putSerializable("org", org);
                break;
            case 1:
                fragmet = new GistsPagerFragment();
                break;
            case 2:
                fragmet = new IssueDashboardPagerFragment();
                break;
            case 3:
                fragmet = new FiltersViewFragment();
                break;
            default:
                fragmet = new HomePagerFragment();
                args.putSerializable("org", orgs.get(position-5));
                break;
        }
        fragmet.setArguments(args);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container,fragmet).commit();
    }

}
