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

import android.content.Intent;
import android.text.TextUtils;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.EXTRA_SUBJECT;
import static android.content.Intent.EXTRA_TEXT;

/**
 * Utilities for creating a share intent
 */
public class ShareUtils {

    /**
     * Create intent with subject and body
     *
     * @param subject
     * @param body
     * @return intent
     */
    public static Intent create(final CharSequence subject,
            final CharSequence body) {
        Intent intent = new Intent(ACTION_SEND);
        intent.setType("text/plain");
        if (!TextUtils.isEmpty(subject))
            intent.putExtra(EXTRA_SUBJECT, subject);
        intent.putExtra(EXTRA_TEXT, body);
        return intent;
    }

    /**
     * Get body from intent
     *
     * @param intent
     * @return body
     */
    public static String getBody(final Intent intent) {
        return intent != null ? intent.getStringExtra(EXTRA_TEXT) : null;
    }

    /**
     * Get subject from intent
     *
     * @param intent
     * @return subject
     */
    public static String getSubject(final Intent intent) {
        return intent != null ? intent.getStringExtra(EXTRA_SUBJECT) : null;
    }
}
