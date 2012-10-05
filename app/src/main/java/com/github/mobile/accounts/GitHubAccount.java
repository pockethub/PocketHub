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
package com.github.mobile.accounts;

/**
 * GitHub account model
 */
public class GitHubAccount {

    /**
     * Account username
     */
    public final String username;

    /**
     * Account password
     */
    public final String password;

    /**
     * Account OAuth token
     */
    public final String authToken;

    /**
     * Create account with username and password
     *
     * @param username
     * @param password
     * @param authToken
     */
    public GitHubAccount(String username, String password, String authToken) {
        this.username = username;
        this.password = password;
        this.authToken = authToken;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + username + ']';
    }
}
