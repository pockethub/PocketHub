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

package com.github.pockethub.android.ui.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.accounts.AccountUtils;
import com.github.pockethub.android.ui.TabPagerFragment;
import com.github.pockethub.android.util.PreferenceUtils;

public class HomePagerFragment extends TabPagerFragment<HomePagerAdapter> {

    private static final String TAG = "HomePagerFragment";

    private static final String PREF_ORG_ID = "orgId";

    private SharedPreferences sharedPreferences;

    private boolean isDefaultUser;

    private User org;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        setOrg((User) getArguments().getParcelable("org"));
    }

    @SuppressLint("CommitPrefEdits")
    private void setOrg(User org) {
        PreferenceUtils.save(sharedPreferences.edit().putInt(PREF_ORG_ID, org.id()));
        this.org = org;
        this.isDefaultUser = AccountUtils.isUser(getActivity(), org);
        configureTabPager();
    }

    @Override
    protected HomePagerAdapter createAdapter() {
        return new HomePagerAdapter(this, isDefaultUser, org);
    }
}
