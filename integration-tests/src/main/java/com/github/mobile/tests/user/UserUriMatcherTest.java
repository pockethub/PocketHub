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

import android.net.Uri;
import android.test.AndroidTestCase;

import com.github.mobile.core.user.UserUriMatcher;

import org.eclipse.egit.github.core.User;

/**
 * Unit tests of {@link UserUriMatcher}
 */
public class UserUriMatcherTest extends AndroidTestCase {

    /**
     * Verify empty URI
     */
    public void testEmptyUri() {
        assertNull(UserUriMatcher.getUser(Uri.parse("")));
    }

    /**
     * Verify no name
     */
    public void testUriWithNoName() {
        assertNull(UserUriMatcher.getUser(Uri.parse("http://github.com")));
        assertNull(UserUriMatcher.getUser(Uri.parse("https://github.com")));
        assertNull(UserUriMatcher.getUser(Uri.parse("http://github.com/")));
        assertNull(UserUriMatcher.getUser(Uri.parse("http://github.com//")));
    }

    /**
     * Verify URI with name
     */
    public void testHttpUriWithName() {
        User user = UserUriMatcher.getUser(Uri
                .parse("http://github.com/defunkt"));
        assertNotNull(user);
        assertEquals("defunkt", user.getLogin());
    }

    /**
     * Verify URI with name
     */
    public void testHttpsUriWithName() {
        User user = UserUriMatcher.getUser(Uri
                .parse("https://github.com/mojombo"));
        assertNotNull(user);
        assertEquals("mojombo", user.getLogin());
    }

    /**
     * Verify URI with name
     */
    public void testUriWithTrailingSlash() {
        User user = UserUriMatcher.getUser(Uri
                .parse("http://github.com/defunkt/"));
        assertNotNull(user);
        assertEquals("defunkt", user.getLogin());
    }

    /**
     * Verify URI with name
     */
    public void testUriWithTrailingSlashes() {
        User user = UserUriMatcher.getUser(Uri
                .parse("http://github.com/defunkt//"));
        assertNotNull(user);
        assertEquals("defunkt", user.getLogin());
    }
}
