package com.github.pockethub.android;

import android.app.Application;

import com.github.pockethub.android.core.gist.GistStore;
import com.github.pockethub.android.dagger.ActivityBuilder;
import com.github.pockethub.android.dagger.DialogFragmentBuilder;
import com.github.pockethub.android.dagger.ServiceBuilder;

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
        ServiceBuilder.class,
        DialogFragmentBuilder.class,
        GitHubModule.class
})
public interface ApplicationComponent extends AndroidInjector<PocketHub> {

    GistStore gistStore();

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<PocketHub> {

        @BindsInstance
        abstract Builder application(Application application);

        @Override
        public abstract ApplicationComponent build();
    }
}