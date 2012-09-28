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
package com.github.mobile.core.user;

import com.github.mobile.core.UrlMatcher;
import com.github.mobile.core.repo.RepositoryUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for user URLs that provides the login matched
 * <p>
 * This class is not thread-safe
 */
public class UserUrlMatcher extends UrlMatcher {

    private static final String REGEX = "^https?://[^/]+/([^/]+)$";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private final Matcher matcher = PATTERN.matcher("");

    /**
     * Get login from URL
     *
     * @param url
     * @return login or null if the given URL is not to a user
     */
    public String getLogin(final String url) {
        if (!isMatch(url, matcher))
            return null;

        String login = matcher.group(1);
        if (RepositoryUtils.isValidOwner(login))
            return login;
        else
            return null;
    }
}
