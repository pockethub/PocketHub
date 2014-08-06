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
package com.github.mobile.tests.user;

import android.test.AndroidTestCase;

import com.github.mobile.core.user.UserUrlMatcher;

/**
 * Tests of {@link UserUrlMatcher}
 */
public class UserUrlMatcherTest extends AndroidTestCase {

    /**
     * Verify empty url
     */
    public void testEmptyUrl() {
        UserUrlMatcher matcher = new UserUrlMatcher();
        assertNull(matcher.getLogin(""));
    }

    /**
     * Verify no name
     */
    public void testUriWithNoName() {
        UserUrlMatcher matcher = new UserUrlMatcher();
        assertNull(matcher.getLogin("http://github.com"));
        assertNull(matcher.getLogin("https://github.com"));
        assertNull(matcher.getLogin("http://github.com/"));
        assertNull(matcher.getLogin("http://github.com//"));
    }

    /**
     * Verify url with name
     */
    public void testHttpUriWithName() {
        UserUrlMatcher matcher = new UserUrlMatcher();
        assertEquals("defunkt", matcher.getLogin("http://github.com/defunkt"));
    }

    /**
     * Verify url with name
     */
    public void testHttpsUriWithName() {
        UserUrlMatcher matcher = new UserUrlMatcher();
        assertEquals("mojombo", matcher.getLogin("https://github.com/mojombo"));
    }

    /**
     * Verify url with repository
     */
    public void testHttpUriWithRepository() {
        UserUrlMatcher matcher = new UserUrlMatcher();
        assertNull(matcher.getLogin("http://github.com/defunkt/resque"));
    }

    /**
     * Verify blacklisted login
     */
    public void testBlacklisted() {
        UserUrlMatcher matcher = new UserUrlMatcher();
        assertNull(matcher.getLogin("http://github.com/blog"));
    }
}
