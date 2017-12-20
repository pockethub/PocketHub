package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.issue.IssuesFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface IssueBrowseFragmentProvider {

    @ContributesAndroidInjector
    IssuesFragment issuesFragment();
}
