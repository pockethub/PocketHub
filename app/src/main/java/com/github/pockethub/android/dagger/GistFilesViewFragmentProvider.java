package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.gist.GistFileFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface GistFilesViewFragmentProvider {

    @ContributesAndroidInjector
    GistFileFragment gistFileFragment();
}
