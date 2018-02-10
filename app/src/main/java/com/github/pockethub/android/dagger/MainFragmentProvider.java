package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.gist.GistsPagerFragment;
import com.github.pockethub.android.ui.issue.FilterListFragment;
import com.github.pockethub.android.ui.issue.IssueDashboardPagerFragment;
import com.github.pockethub.android.ui.user.HomePagerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public interface MainFragmentProvider {

    @ContributesAndroidInjector(modules = HomePagerFragmentProvider.class)
    HomePagerFragment homePagerFragment();

    @ContributesAndroidInjector(modules = GistsPagerFragmentProvider.class)
    GistsPagerFragment gistsPagerFragment();

    @ContributesAndroidInjector(modules = IssueDashboardPagerFragmentProvider.class)
    IssueDashboardPagerFragment issueDashboardPagerFragment();

    @ContributesAndroidInjector
    FilterListFragment filterListFragment();
}
