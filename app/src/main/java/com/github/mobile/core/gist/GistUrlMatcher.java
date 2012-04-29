/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.core.gist;

import com.github.mobile.core.UrlMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for gist URLS that provides the Gist id matched
 * <p>
 * This class is not thread-safe
 */
public class GistUrlMatcher extends UrlMatcher {

    private static final String REGEX = "https?://((gist.github.com)|([^/]+/gist))/([a-fA-F0-9]+)";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private final Matcher matcher = PATTERN.matcher("");

    /**
     * Get Gist id from URL
     *
     * @param url
     * @return gist id or null if the given URL is not to a Gist
     */
    public String getId(final String url) {
        return isMatch(url, matcher) ? matcher.group(4) : null;
    }
}
