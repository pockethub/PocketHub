package com.github.pockethub.android.dagger;

import com.github.pockethub.android.sync.SyncAdapterService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public interface ServiceBuilder {

    @ContributesAndroidInjector
    SyncAdapterService provideSyncAdapterService();
}
