package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.commit.CommitCompareListFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface CommitCompareViewFragmentProvider {

    @ContributesAndroidInjector
    CommitCompareListFragment commitCompareListFragment();
}
