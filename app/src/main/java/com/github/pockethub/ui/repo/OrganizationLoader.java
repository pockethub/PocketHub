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
package com.github.pockethub.ui.repo;

import android.accounts.Account;
import android.app.Activity;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Organization;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.R;
import com.github.pockethub.accounts.AuthenticatedUserLoader;
import com.github.pockethub.core.user.UserComparator;
import com.github.pockethub.persistence.AccountDataManager;
import com.github.pockethub.util.ToastUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Load of a {@link List} or {@link User} organizations
 */
public class OrganizationLoader extends AuthenticatedUserLoader<List<Organization>> {

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
    protected List<Organization> getAccountFailureData() {
        return Collections.emptyList();
    }

    @Override
    public List<Organization> load(final Account account) {
        List<Organization> orgs;
        try {
            orgs = accountDataManager.getOrgs(false);
        } catch (final IOException e) {
            Log.e(TAG, "Exception loading organizations", e);
            ToastUtils.show(activity, e, R.string.error_orgs_load);
            return Collections.emptyList();
        }
        Collections.sort(orgs, userComparatorProvider.get());
        return orgs;
    }
}
