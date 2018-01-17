package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.repo.RepositoryContributorsFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface RepositoryContributorsFragmentProvider {

    @ContributesAndroidInjector
    RepositoryContributorsFragment repositoryContributorsFragment();
}
