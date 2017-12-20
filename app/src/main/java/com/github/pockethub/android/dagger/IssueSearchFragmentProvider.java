package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.issue.SearchIssueListFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface IssueSearchFragmentProvider {

    @ContributesAndroidInjector
    SearchIssueListFragment searchIssueListFragment();
}
