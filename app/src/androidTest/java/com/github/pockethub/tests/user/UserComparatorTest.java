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
package com.github.pockethub.tests.user;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.test.AndroidTestCase;

import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.accounts.GitHubAccount;
import com.github.pockethub.core.user.UserComparator;

/**
 * Unit tests of {@link UserComparator}
 */
public class UserComparatorTest extends AndroidTestCase {

    /**
     * Test sorting of users that match login
     */
    public void testLoginMatch() {
        GitHubAccount account = new GitHubAccount(new Account("m", "t"),
                AccountManager.get(getContext()));
        UserComparator comparator = new UserComparator(account);

        assertTrue(comparator.compare(createUser("m"),
                createUser("a")) < 0);
        assertTrue(comparator.compare(createUser("a"),
                createUser("m")) > 0);
        assertTrue(comparator.compare(createUser("m"),
                createUser("z")) < 0);
        assertTrue(comparator.compare(createUser("z"),
                createUser("m")) > 0);
        assertEquals(
                0,
                comparator.compare(createUser("m"),
                        createUser("m")));
    }

    /**
     * Test sorting of users that don't match login
     */
    public void testNoLoginMatch() {
        GitHubAccount account = new GitHubAccount(new Account("m", "t"),
                AccountManager.get(getContext()));
        UserComparator comparator = new UserComparator(account);

        assertTrue(comparator.compare(createUser("a"),
                createUser("c")) < 0);
        assertTrue(comparator.compare(createUser("db"),
                createUser("da")) > 0);
    }

    private User createUser(String login){
        User user = new User();
        user.login = login;
        return user;
    }
}
