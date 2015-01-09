package com.github.mobile.ui.user;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.github.mobile.accounts.AccountUtils;
import com.github.mobile.ui.TabPagerFragment;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.egit.github.core.User;

public class HomePagerFragment extends TabPagerFragment<HomePagerAdapter>
    implements OrganizationSelectionProvider {

    private static final String TAG = "HomePagerFragment";

    private static final String PREF_ORG_ID = "orgId";

    private Set<OrganizationSelectionListener> orgSelectionListeners = new LinkedHashSet<>();

    private boolean isDefaultUser;

    private User org;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        org = (User) getArguments().getSerializable("org");
        setOrg(org);
    }

    private void setOrg(User org) {
        Log.d(TAG, "setOrg : " + org.getLogin());

/*        PreferenceUtils.save(sharedPreferences.edit().putInt(PREF_ORG_ID,
            org.getId()));

        // Don't notify listeners or change pager if org hasn't changed
        if (this.org != null && this.org.getId() == org.getId())
            return;*/

        this.org = org;

        boolean isDefaultUser = AccountUtils.isUser(getActivity(), org);
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
    protected HomePagerAdapter createAdapter() {
        return new HomePagerAdapter((android.support.v7.app.ActionBarActivity) getActivity(), isDefaultUser);
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
}
