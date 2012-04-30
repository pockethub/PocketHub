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

import com.github.mobile.core.GitHubAccount;
import com.google.inject.Inject;

import java.util.Comparator;

import org.eclipse.egit.github.core.User;

/**
 * Sorts users and orgs in alphabetical order with special handling to put currently authenticated user first.
 */
public class UserComparator implements Comparator<User> {

    private final String login;

    /**
     * Create comparator for given account
     *
     * @param account
     */
    @Inject
    public UserComparator(final GitHubAccount account) {
        login = account.username;
    }

    @Override
    public int compare(final User lhs, final User rhs) {
        final String lhsLogin = lhs.getLogin();
        if (lhsLogin.equals(login))
            return -1;

        final String rhsLogin = rhs.getLogin();
        if (rhsLogin.equals(login))
            return 1;

        return lhsLogin.compareToIgnoreCase(rhsLogin);
    }
}
