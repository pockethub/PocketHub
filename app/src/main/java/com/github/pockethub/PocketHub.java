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

package com.github.pockethub;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.alorma.gitskarios.core.client.LogProvider;
import com.alorma.gitskarios.core.client.LogProviderInterface;
import com.alorma.gitskarios.core.client.TokenProvider;
import com.alorma.gitskarios.core.client.TokenProviderInterface;
import com.alorma.gitskarios.core.client.UrlProvider;
import com.alorma.gitskarios.core.client.UrlProviderInterface;
import com.alorma.gitskarios.core.client.UsernameProvider;
import com.alorma.gitskarios.core.client.UsernameProviderInterface;
import com.bugsnag.android.Bugsnag;
import com.github.pockethub.accounts.StoreCredentials;

import net.danlew.android.joda.JodaTimeAndroid;

public class PocketHub extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        Bugsnag.init(this);
        Bugsnag.setNotifyReleaseStages("production");

        TokenProvider.setTokenProviderInstance(new TokenProviderInterface() {
            @Override
            public String getToken() {
                return getStoreCredentials().token();
            }
        });

        UrlProvider.setUrlProviderInstance(new UrlProviderInterface() {
            @Override
            public String getUrl() {
                return getStoreCredentials().getUrl();
            }
        });

        UsernameProvider.setUsernameProviderInterface(new UsernameProviderInterface() {
            @Override
            public String getUsername() {
                return getStoreCredentials().getUserName();
            }
        });

        LogProvider.setTokenProviderInstance(new LogProviderInterface() {
            @Override
            public void log(String message) {
                if (BuildConfig.DEBUG) {
                    Log.v("RetrofitLog", message);
                }
            }
        });
    }

    @NonNull
    private StoreCredentials getStoreCredentials() {
        return new StoreCredentials(this);
    }
}
