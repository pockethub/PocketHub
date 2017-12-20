package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.issue.DashboardIssueFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface IssueDashboardPagerFragmentProvider {

    @ContributesAndroidInjector
    DashboardIssueFragment dashboardIssueFragment();
}
