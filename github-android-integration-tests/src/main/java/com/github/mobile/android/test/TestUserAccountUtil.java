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

package com.github.mobile.android.test;

import static com.github.mobile.android.authenticator.Constants.AUTHTOKEN_TYPE;
import static com.github.mobile.android.authenticator.Constants.GITHUB_ACCOUNT_TYPE;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

import com.github.mobile.android.tests.R;

import java.util.regex.Pattern;

public class TestUserAccountUtil {

    private static final String TAG = "TestUserAccountUtil";
    private static Pattern EMPTY_CONF_REGEX = Pattern.compile("\\$\\{.*\\}");

    /**
     * Checks the device has a valid GitHub account, if not, adds one using the test credentials
     * found in system properties 'github.test.account.username' and 'github.test.account.password'.
     *
     * The credentials should be configured in your .m2/settings.xml - and obviously, don't
     * use a 'real' user account.
     *
     * @param instrumentation taken from the test context
     * @return true if valid account credentials are available
     */
    public static boolean ensureValidGitHubAccountAvailable(Instrumentation instrumentation) {
        Context c = instrumentation.getContext();
        AccountManager accountManager = AccountManager.get(instrumentation.getTargetContext());

        for (Account account : accountManager.getAccountsByType(GITHUB_ACCOUNT_TYPE)) {
            if (accountManager.peekAuthToken(account, AUTHTOKEN_TYPE) != null) {
                Log.i(TAG, "Using existing account : "+account.name);
                return true; // we have a valid account that has successfully authenticated
            }
        }

        String testAccountUsername = c.getString(R.string.test_account_username);
        String testAccountPassword = c.getString(R.string.test_account_password);

        if (EMPTY_CONF_REGEX.matcher(testAccountUsername).matches()) {
            Log.w(TAG, "No valid test account username  : "+testAccountUsername);
            return false;
        }

        Log.i(TAG, "Adding test account using supplied credentials : username=" + testAccountUsername);
        Account account = new Account(testAccountUsername, GITHUB_ACCOUNT_TYPE);
        accountManager.addAccountExplicitly(account, testAccountPassword, null);
        accountManager.setAuthToken(account, AUTHTOKEN_TYPE, testAccountPassword);
        return true;
    }
}
