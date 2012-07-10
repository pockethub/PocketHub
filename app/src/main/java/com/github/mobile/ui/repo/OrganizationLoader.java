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
package com.github.mobile.ui.repo;

import android.app.Activity;
import android.util.Log;

import com.github.mobile.R.string;
import com.github.mobile.accounts.AuthenticatedUserLoader;
import com.github.mobile.core.user.UserComparator;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.User;

/**
 * Load of a {@link List} or {@link User} organizations
 */
public class OrganizationLoader extends AuthenticatedUserLoader<List<User>> {

    private static final String TAG = "OrganizationLoader";

    private final Provider<UserComparator> userComparatorProvider;

    private final AccountDataManager accountDataManager;

    /**
     * Create organization loader
     *
     * @param activity
     * @param accountDataManager
     * @param userComparatorProvider
     */
    @Inject
    public OrganizationLoader(Activity activity,
            AccountDataManager accountDataManager,
            Provider<UserComparator> userComparatorProvider) {
        super(activity);

        this.accountDataManager = accountDataManager;
        this.userComparatorProvider = userComparatorProvider;
    }

    @Override
    protected List<User> getAccountFailureData() {
        return Collections.emptyList();
    }

    @Override
    public List<User> load() {
        List<User> orgs;
        try {
            orgs = accountDataManager.getOrgs();
        } catch (final IOException e) {
            Log.e(TAG, "Exception loading organizations", e);
            ToastUtils.show(activity, e, string.error_orgs_load);
            return Collections.emptyList();
        }
        Collections.sort(orgs, userComparatorProvider.get());
        return orgs;
    }
}
