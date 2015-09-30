package com.github.pockethub.ui.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.os.ParcelableCompat;
import android.view.View;

import com.alorma.github.sdk.bean.dto.response.Organization;
import com.alorma.github.sdk.bean.dto.response.UserType;
import com.github.pockethub.accounts.AccountUtils;
import com.github.pockethub.ui.TabPagerFragment;
import com.github.pockethub.util.PreferenceUtils;

import com.alorma.github.sdk.bean.dto.response.User;

import java.util.Date;

public class HomePagerFragment extends TabPagerFragment<HomePagerAdapter> {

    private static final String TAG = "HomePagerFragment";

    private static final String PREF_ORG_ID = "orgId";

    private SharedPreferences sharedPreferences;

    private boolean isDefaultUser;

    private Organization org;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        setOrg((Organization) getArguments().getParcelable("org"));
    }

    private void setOrg(Organization org) {
        PreferenceUtils.save(sharedPreferences.edit().putInt(PREF_ORG_ID, org.id));
        this.org = org;
        this.isDefaultUser = AccountUtils.isUser(getActivity(), org);
        configureTabPager();
    }

    @Override
    protected HomePagerAdapter createAdapter() {
        return new HomePagerAdapter(this, isDefaultUser, orgToUser(org));
    }

    private User orgToUser(Organization org){
        User user = new User();
        user.id = org.id;
        user.login = org.login;
        user.name = org.name;
        user.company = org.company;

        user.created_at = org.created_at;
        user.updated_at = org.updated_at;

        user.avatar_url = org.avatar_url;
        user.gravatar_id = org.gravatar_id;
        user.blog = org.blog;
        user.bio = org.bio;
        user.email = org.email;

        user.location = org.location;
        user.type = org.type;

        user.site_admin = org.site_admin;

        user.public_repos = org.public_repos;
        user.public_gists = org.public_gists;
        user.followers = org.followers;
        user.following = org.following;
        return user;
    }
}
