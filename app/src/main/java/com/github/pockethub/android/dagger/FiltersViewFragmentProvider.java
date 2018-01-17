package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.issue.FilterListFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface FiltersViewFragmentProvider {

    @ContributesAndroidInjector
    FilterListFragment filterListFragment();
}
