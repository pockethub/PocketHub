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
package com.github.pockethub.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import static android.content.Context.MODE_PRIVATE;

/**
 * Utility class for working with {@link SharedPreferences}
 */
public class PreferenceUtils {

    /**
     * Preference to wrap lines of code
     */
    public static final String WRAP = "wrap";

    /**
     * Preference to render markdown
     */
    public static final String RENDER_MARKDOWN = "renderMarkdown";

    /**
     * Get code browsing preferences
     *
     * @param context
     * @return preferences
     */
    public static SharedPreferences getCodePreferences(final Context context) {
        return context.getSharedPreferences("code", MODE_PRIVATE);
    }

    /**
     * Save preferences in given editor
     *
     * @param editor
     */
    public static void save(final Editor editor) {
        editor.apply();
    }
}
