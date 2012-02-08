package com.github.mobile.android.repo;

import static android.util.Log.DEBUG;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.android.AccountDataManager;
import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.ErrorHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;

/**
 * Fragment to load a list of GitHub organizations
 */
public class OrgListFragment extends ListLoadingFragment<User> implements Comparator<User> {

    private static final String TAG = "OLF";

    @Inject
    private AccountDataManager cache;

    @Inject
    private AvatarHelper avatarHelper;

    @Inject
    private GitHubClient client;

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader<List<User>>(getActivity()) {

            public List<User> loadInBackground() {
                try {
                    List<User> orgs = cache.getOrgs();
                    Collections.sort(orgs, OrgListFragment.this);
                    return orgs;
                } catch (final IOException e) {
                    if (Log.isLoggable(TAG, DEBUG))
                        Log.d(TAG, "Exception loading organizations", e);

                    getActivity().runOnUiThread(new Runnable() {

                        public void run() {
                            ErrorHelper.show(getContext(), e, string.error_orgs_load);
                        }
                    });

                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    protected ListAdapter adapterFor(List<User> items) {
        return new ViewHoldingListAdapter<User>(items, ViewInflator.viewInflatorFor(getActivity(), layout.org_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(OrgViewHolder.class, avatarHelper));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        User user = (User) l.getItemAtPosition(position);
        startActivity(RepoBrowseActivity.createIntent(user));
    }

    @Override
    public int compare(final User lhs, final User rhs) {
        if (lhs.getLogin().equals(client.getUser()))
            return -1;
        if (rhs.getLogin().equals(client.getUser()))
            return 1;

        return lhs.getLogin().compareToIgnoreCase(rhs.getLogin());
    }
}
