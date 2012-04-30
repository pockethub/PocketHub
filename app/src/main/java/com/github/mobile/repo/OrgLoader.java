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
package com.github.mobile.repo;

import static android.util.Log.WARN;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;
import android.app.Activity;
import android.util.Log;

import com.github.mobile.R.string;
import com.github.mobile.async.AuthenticatedUserLoader;
import com.github.mobile.core.user.UserComparator;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.User;

public class OrgLoader extends AuthenticatedUserLoader<List<User>> {

    private static final String TAG = "GH.UAOL";

    private final Provider<UserComparator> userComparatorProvider;

    private final AccountDataManager accountDataManager;

    @Inject
    public OrgLoader(Activity activity, AccountDataManager accountDataManager,
            Provider<UserComparator> userComparatorProvider) {
        super(activity);
        this.accountDataManager = accountDataManager;
        this.userComparatorProvider = userComparatorProvider;
    }

    public List<User> load() {
        Log.d(TAG, "Going to load organizations");
        try {
            List<User> orgs = accountDataManager.getOrgs();
            sort(orgs, userComparatorProvider.get());
            return orgs;
        } catch (final IOException e) {
            if (Log.isLoggable(TAG, WARN))
                Log.w(TAG, "Exception loading organizations", e);

            ToastUtils.show(activity, string.error_orgs_load);

            return emptyList();
        }
    }
}
