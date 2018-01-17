package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.issue.IssueFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface IssuesViewFragmentProvider {

    @ContributesAndroidInjector
    IssueFragment issueFragment();
}
