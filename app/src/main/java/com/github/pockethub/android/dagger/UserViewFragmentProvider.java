package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.repo.UserRepositoryListFragment;
import com.github.pockethub.android.ui.user.UserCreatedNewsFragment;
import com.github.pockethub.android.ui.user.UserFollowersFragment;
import com.github.pockethub.android.ui.user.UserFollowingFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface UserViewFragmentProvider {

    @ContributesAndroidInjector
    UserCreatedNewsFragment userCreatedNewsFragment();

    @ContributesAndroidInjector
    UserRepositoryListFragment userRepositoryListFragment();

    @ContributesAndroidInjector
    UserFollowersFragment userFollowersFragment();

    @ContributesAndroidInjector
    UserFollowingFragment userFollowingFragment();

}
