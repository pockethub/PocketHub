/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.pockethub.android;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.multidex.MultiDex;
import com.bugsnag.android.Bugsnag;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;

public class PocketHub extends DaggerApplication {

    private ApplicationComponent applicationComponent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (!"robolectric".equals(Build.FINGERPRINT)) {
            MultiDex.install(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        Bugsnag.init(this);
        Bugsnag.setNotifyReleaseStages("production");
    }

    @Inject
    void logInjection() {
        Log.i("Test", "Injecting " + PocketHub.class.getSimpleName());
    }

    public ApplicationComponent applicationComponent() {
        return applicationComponent;
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        applicationComponent = (ApplicationComponent) DaggerApplicationComponent.builder()
                .application(this)
                .create(this);

        return applicationComponent;
    }
}
