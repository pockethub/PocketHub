package com.github.mobile.android.repo;

import static android.util.Log.WARN;
import static com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Collections.sort;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.github.mobile.android.async.AuthenticatedUserLoader;
import com.github.mobile.android.persistence.AccountDataManager;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.github.mobile.android.util.AvatarHelper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.User;

/**
 * Fragment to load a list of GitHub organizations
 */
public class OrgListFragment extends ListLoadingFragment<User> {

    private static final String TAG = "GH.OLF";

    @Inject
    private AccountDataManager cache;

    @Inject
    private AvatarHelper avatarHelper;

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return new AuthenticatedUserLoader<List<User>>(getActivity()) {
            @Inject Provider<UserComparator> userComparatorProvider;

            public List<User> load() {
                Log.d(TAG, "Going to load organizations");
                try {
                    List<User> orgs = cache.getOrgs();
                    sort(orgs, userComparatorProvider.get());
                    return orgs;
                } catch (final IOException e) {
                    if (Log.isLoggable(TAG, WARN))
                        Log.w(TAG, "Exception loading organizations", e);

                    showError(e, string.error_orgs_load);

                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        super.onCreateOptionsMenu(optionsMenu, inflater);
        optionsMenu.findItem(id.refresh).setShowAsAction(SHOW_AS_ACTION_NEVER);
    }

    @Override
    protected ViewHoldingListAdapter<User> adapterFor(List<User> items) {
        return new ViewHoldingListAdapter<User>(items, ViewInflator.viewInflatorFor(getActivity(), layout.org_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(OrgViewHolder.class, avatarHelper));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        User user = (User) l.getItemAtPosition(position);
        startActivity(RepoBrowseActivity.createIntent(user));
    }

}
