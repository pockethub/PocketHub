package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.gist.MyGistsFragment;
import com.github.pockethub.android.ui.gist.PublicGistsFragment;
import com.github.pockethub.android.ui.gist.StarredGistsFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface GistsPagerFragmentProvider {

    @ContributesAndroidInjector
    MyGistsFragment myGistsFragment();

    @ContributesAndroidInjector
    PublicGistsFragment publicGistsFragment();

    @ContributesAndroidInjector
    StarredGistsFragment starredGistsFragment();
}
