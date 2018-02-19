package com.github.pockethub.android;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Module;

@Module
abstract class ApplicationModule {

    @Binds
    @Singleton
    abstract Context provideApplicationContext(Application application);
}
