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
package com.github.pockethub.core;

import android.text.TextUtils;

import java.util.regex.Matcher;

/**
 * Base URL matcher with utilities for sub-classes to use
 */
public abstract class UrlMatcher {

    /**
     * Is given input URL a match?
     * <p>
     * This method ignores null and empty URLs and does not reset the matcher
     * with them
     *
     * @param url
     * @param matcher
     * @return true if matcher matches, false otherwise
     */
    protected boolean isMatch(final String url, final Matcher matcher) {
        return !TextUtils.isEmpty(url) && matcher.reset(url).matches();
    }
}
