package com.github.pockethub.android;

import android.app.Application;

import com.github.pockethub.android.dagger.ActivityBuilder;
import com.github.pockethub.android.dagger.DialogFragmentBuilder;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        ApplicationModule.class,
        PocketHubModule.class,
        ActivityBuilder.class,
        DialogFragmentBuilder.class,
        GitHubModule.class
})
interface ApplicationComponent extends AndroidInjector<PocketHub> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<PocketHub> {

        @BindsInstance
        abstract Builder application(Application application);
    }
}