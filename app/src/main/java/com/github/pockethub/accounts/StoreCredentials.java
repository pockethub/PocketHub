/*
 * Copyright (c) 2016 PocketHub
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

package com.github.pockethub.accounts;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StoreCredentials {

    public static final String KEY_URL = "KEY_URL";
    private static final String USER_NAME = StoreCredentials.class.getSimpleName() + ".USER_NAME";
    private static final String USER_TOKEN = StoreCredentials.class.getSimpleName() + ".USER_TOKEN";
    private final SharedPreferences.Editor editor;
    private final SharedPreferences preferences;

    public StoreCredentials(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        editor = preferences.edit();
    }

    public void storeToken(String accessToken) {
        editor.putString(USER_TOKEN, accessToken);
        editor.apply();
    }

    public String token() {
        return preferences.getString(USER_TOKEN, null);
    }

    public void clear() {
        editor.remove(KEY_URL);
        editor.remove(USER_NAME);
        editor.remove(USER_TOKEN);
        editor.commit();
    }

    public void storeUsername(String name) {
        editor.putString(USER_NAME, name);
        editor.apply();
    }

    public void storeUrl(String url) {
        editor.putString(KEY_URL, url);
        editor.apply();
    }

    public String getUserName() {
        return preferences.getString(USER_NAME, null);
    }

    public String getUrl() {
        return preferences.getString(KEY_URL, null);
    }
}
