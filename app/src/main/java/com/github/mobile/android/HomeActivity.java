package com.github.mobile.android;

import static com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_LIST;
import static com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import static com.github.mobile.android.R.menu;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import static com.google.common.collect.Lists.newArrayList;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.gist.GistsActivity;
import com.github.mobile.android.issue.FilterBrowseActivity;
import com.github.mobile.android.issue.IssueDashboardActivity;
import com.github.mobile.android.persistence.AccountDataManager;
import com.github.mobile.android.repo.OrgViewHolder;
import com.github.mobile.android.repo.UserAndOrgLoader;
import com.github.mobile.android.repo.UserComparator;
import com.github.mobile.android.ui.user.UserPagerAdapter;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.madgag.android.listviews.ViewFactory;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectView;

/**
 * Activity to view a org's various pages
 */
public class HomeActivity extends RoboSherlockFragmentActivity
        implements OnNavigationListener, LoaderCallbacks<List<User>> {

    private static final String TAG = "GH.UVA";

    @Inject
    private AccountDataManager accountDataManager;

    @Inject
    private Provider<UserComparator> userComparatorProvider;

    private List<User> users = Collections.emptyList();
    private ViewHoldingListAdapter<User> userOrOrgListAdapter;
    private List<UserOrOrgSelectionListener> userOrOrgSelectionListeners = newArrayList();

    /**
     * Create intent for this activity
     *
     * @param user
     * @return intent
     */
    public static Intent createIntent(User user) {
        return new Builder("org.VIEW").add(EXTRA_USER, user).toIntent();
    }

    private User org;

    @InjectView(id.tpi_header)
    private TitlePageIndicator indicator;

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @Inject
    private UserAndOrgLoader userAndOrgLoader;

    @Inject
    private AvatarHelper avatarHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager_with_title);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(NAVIGATION_MODE_LIST);

        getSupportLoaderManager().initLoader(0, null, this);
        ViewHolderFactory<User> userViewHolderFactory = reflectiveFactoryFor(OrgViewHolder.class, avatarHelper);
        ViewFactory<User> selectedUserViewFactory = new ViewFactory<User>(viewInflatorFor(this, layout.org_item),
                userViewHolderFactory);
        ViewFactory<User> dropDownViewFactory = new ViewFactory<User>(viewInflatorFor(this,
                layout.org_item_dropdown), userViewHolderFactory);
        userOrOrgListAdapter = new ViewHoldingListAdapter<User>(users, selectedUserViewFactory, dropDownViewFactory);
        getSupportActionBar().setListNavigationCallbacks(
                userOrOrgListAdapter, this);

        User org = (User) getIntent().getSerializableExtra(EXTRA_USER);
        if (org != null)
            setOrg(org);
    }

    private void setOrg(User org) {
        Log.d(TAG, "setOrg : " + org.getLogin());

        this.org = org;

        if (pager.getAdapter() == null) {
            pager.setAdapter(new UserPagerAdapter(getSupportFragmentManager()));
            indicator.setViewPager(pager);
        }

        for (UserOrOrgSelectionListener listener : userOrOrgSelectionListeners)
            listener.onUserOrOrgSelected(org);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getSupportMenuInflater().inflate(menu.welcome, optionMenu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case id.dashboard_issues:
                startActivity(new Intent(this, IssueDashboardActivity.class));
                return true;
            case id.gists:
                startActivity(new Intent(this, GistsActivity.class));
                return true;
            case id.search:
                onSearchRequested();
                return true;
            case id.bookmarks:
                startActivity(FilterBrowseActivity.createIntent());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        setOrg(users.get(itemPosition));
        return true;
    }

    @Override
    public Loader<List<User>> onCreateLoader(int i, Bundle bundle) {
        return new UserAndOrgLoader(this, accountDataManager, userComparatorProvider);
    }

    @Override
    public void onLoadFinished(Loader<List<User>> listLoader, List<User> users) {
        this.users = users;

        userOrOrgListAdapter.setList(users);
        if (org != null)
            for (int i = 0; i < users.size(); ++i)
                if (org.getId() == users.get(i).getId()) {
                    getSupportActionBar().setSelectedNavigationItem(i);
                    break;
                }
    }

    @Override
    public void onLoaderReset(Loader<List<User>> listLoader) {
    }

    public void registerUserOrOrgListener(UserOrOrgSelectionListener listener) {
        userOrOrgSelectionListeners.add(listener);
    }

    public static interface UserOrOrgSelectionListener {
        public void onUserOrOrgSelected(User userOrOrg);
    }

    public static void registerUserOrOrgSelectionListener(Activity activity, UserOrOrgSelectionListener listener) {
        try {
            ((HomeActivity) activity).registerUserOrOrgListener(listener);
        } catch (ClassCastException e) {
            activity.finish();
            throw new ClassCastException(activity.toString() + " must extend HomeActivity");
        }
    }
}
