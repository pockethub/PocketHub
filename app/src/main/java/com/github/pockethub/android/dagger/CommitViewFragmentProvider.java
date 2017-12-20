package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.commit.CommitDiffListFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface CommitViewFragmentProvider {

    @ContributesAndroidInjector
    CommitDiffListFragment commitDiffListFragment();
}
