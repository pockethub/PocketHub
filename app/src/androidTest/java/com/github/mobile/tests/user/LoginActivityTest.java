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

import android.accounts.AccountManager;

import com.github.mobile.accounts.AccountUtils;
import com.github.mobile.accounts.LoginActivity;
import com.github.mobile.tests.ActivityTest;

/**
 * Tests of {@link LoginActivity}
 */
public class LoginActivityTest extends ActivityTest<LoginActivity> {

    /**
     * Create navigation_drawer_header_background for {@link LoginActivity}
     */
    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    /**
     * Verify authenticator is registered
     */
    public void testHasAuthenticator() {
        assertTrue(AccountUtils.hasAuthenticator(AccountManager
            .get(getActivity())));
    }

}
