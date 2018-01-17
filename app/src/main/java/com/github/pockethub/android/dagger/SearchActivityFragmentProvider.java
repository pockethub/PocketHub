package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.search.SearchRepositoryListFragment;
import com.github.pockethub.android.ui.search.SearchUserListFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface SearchActivityFragmentProvider {

    @ContributesAndroidInjector
    SearchRepositoryListFragment searchRepositoryListFragment();

    @ContributesAndroidInjector
    SearchUserListFragment searchUserListFragment();

}
