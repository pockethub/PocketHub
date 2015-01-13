package com.github.mobile.ui.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.github.mobile.accounts.AccountUtils;
import com.github.mobile.ui.TabPagerFragment;
import com.github.mobile.util.PreferenceUtils;

import org.eclipse.egit.github.core.User;

public class HomePagerFragment extends TabPagerFragment<HomePagerAdapter> {

    private static final String TAG = "HomePagerFragment";

    private static final String PREF_ORG_ID = "orgId";

    private SharedPreferences sharedPreferences;

    private boolean isDefaultUser;

    private User org;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        setOrg((User) getArguments().getSerializable("org"));
    }

    private void setOrg(User org) {
        PreferenceUtils.save(sharedPreferences.edit().putInt(PREF_ORG_ID,
            org.getId()));
        this.org = org;
        this.isDefaultUser = AccountUtils.isUser(getActivity(), org);
        configureTabPager();
    }

    @Override
    protected HomePagerAdapter createAdapter() {
        return new HomePagerAdapter(this, isDefaultUser, org);
    }
}
