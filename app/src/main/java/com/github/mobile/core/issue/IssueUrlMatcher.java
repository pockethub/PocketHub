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
package com.github.mobile.core.issue;

import com.github.mobile.core.UrlMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for issue URLS that provides the issue number matched
 * <p>
 * This class is not thread-safe
 */
public class IssueUrlMatcher extends UrlMatcher {

    private static final String REGEX = "https?://.+/[^/]+/[^/]+/(issues|pull)/(issue/)?(\\d+)";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private final Matcher matcher = PATTERN.matcher("");

    /**
     * Get issue number from URL
     *
     * @param url
     * @return issue number of -1 if the given URL is not to an issue
     */
    public int getNumber(final String url) {
        if (!isMatch(url, matcher))
            return -1;

        try {
            return Integer.parseInt(matcher.group(3));
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }
}
