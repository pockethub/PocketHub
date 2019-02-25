package com.github.pockethub.android;

import android.app.Application;
import android.content.Context;
import com.squareup.sqldelight.android.AndroidSqliteDriver;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
abstract class ApplicationModule {

    @Binds
    @Singleton
    abstract Context provideApplicationContext(Application application);

    @Singleton
    @Provides
    static Database provideDatabase(Context context) {
        AndroidSqliteDriver driver = new AndroidSqliteDriver(Database.Schema.INSTANCE, context, "cache.db");
        return new Database(driver);
    }
}
