package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.repo.RepositoryListFragment;
import com.github.pockethub.android.ui.user.MembersFragment;
import com.github.pockethub.android.ui.user.MyFollowersFragment;
import com.github.pockethub.android.ui.user.MyFollowingFragment;
import com.github.pockethub.android.ui.user.OrganizationNewsFragment;
import com.github.pockethub.android.ui.user.UserReceivedNewsFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface HomePagerFragmentProvider {

    @ContributesAndroidInjector
    UserReceivedNewsFragment userReceivedNewsFragment();

    @ContributesAndroidInjector
    OrganizationNewsFragment organizationNewsFragment();

    @ContributesAndroidInjector
    RepositoryListFragment repositoryListFragment();

    @ContributesAndroidInjector
    MyFollowersFragment myFollowersFragment();

    @ContributesAndroidInjector
    MyFollowingFragment myFollowingFragment();

    @ContributesAndroidInjector
    MembersFragment membersFragment();
}
