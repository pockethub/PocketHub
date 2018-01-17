package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.gist.GistFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface GistsViewFragmentProvider {

    @ContributesAndroidInjector
    GistFragment gistFragment();
}
