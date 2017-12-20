package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.code.RepositoryCodeFragment;
import com.github.pockethub.android.ui.commit.CommitListFragment;
import com.github.pockethub.android.ui.issue.IssuesFragment;
import com.github.pockethub.android.ui.repo.RepositoryNewsFragment;
import com.github.pockethub.android.ui.repo.RepositoryReadmeFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface RepositoryViewFragmentProvider {

    @ContributesAndroidInjector
    RepositoryReadmeFragment repositoryReadmeFragment();

    @ContributesAndroidInjector
    RepositoryNewsFragment repositoryNewsFragment();

    @ContributesAndroidInjector
    RepositoryCodeFragment repositoryCodeFragment();

    @ContributesAndroidInjector
    CommitListFragment commitListFragment();

    @ContributesAndroidInjector
    IssuesFragment issuesFragment();
}
