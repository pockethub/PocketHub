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

import com.github.mobile.accounts.GitHubAccount;
import com.github.mobile.core.user.UserComparator;

import org.eclipse.egit.github.core.User;

/**
 * Unit tests of {@link UserComparator}
 */
public class UserComparatorTest extends AndroidTestCase {

    /**
     * Test sorting of users that match login
     */
    public void testLoginMatch() {
        GitHubAccount account = new GitHubAccount("m", "n");
        UserComparator comparator = new UserComparator(account);

        assertTrue(comparator.compare(new User().setLogin("m"),
                new User().setLogin("a")) < 0);
        assertTrue(comparator.compare(new User().setLogin("a"),
                new User().setLogin("m")) > 0);
        assertTrue(comparator.compare(new User().setLogin("m"),
                new User().setLogin("z")) < 0);
        assertTrue(comparator.compare(new User().setLogin("z"),
                new User().setLogin("m")) > 0);
        assertEquals(
                0,
                comparator.compare(new User().setLogin("m"),
                        new User().setLogin("m")));
    }

    /**
     * Test sorting of users that don't match login
     */
    public void testNoLoginMatch() {
        GitHubAccount account = new GitHubAccount("m", "n");
        UserComparator comparator = new UserComparator(account);

        assertTrue(comparator.compare(new User().setLogin("a"),
                new User().setLogin("c")) < 0);
        assertTrue(comparator.compare(new User().setLogin("db"),
                new User().setLogin("da")) > 0);
    }
}
