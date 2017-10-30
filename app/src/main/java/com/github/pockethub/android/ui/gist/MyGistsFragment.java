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
package com.github.pockethub.android.ui.gist;

import android.accounts.Account;
import android.content.Intent;

import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.core.ResourcePager;
import com.github.pockethub.android.core.gist.GistPager;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.service.gists.GistService;
import com.google.inject.Inject;
import com.google.inject.Provider;

import static android.app.Activity.RESULT_OK;
import static com.github.pockethub.android.RequestCodes.GIST_CREATE;
import static com.github.pockethub.android.RequestCodes.GIST_VIEW;

/**
 * Fragment to display a list of Gists
 */
public class MyGistsFragment extends GistsFragment {

    @Inject
    private Provider<Account> accountProvider;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == GIST_CREATE || requestCode == GIST_VIEW)
                && RESULT_OK == resultCode) {
            forceRefresh();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected ResourcePager<Gist> createPager() {
        return new GistPager(store) {

            @Override
            public PageIterator<Gist> createIterator(int page, int size) {
                return new PageIterator<>(page1 ->
                        ServiceGenerator.createService(getActivity(), GistService.class)
                                .getUserGists(page1), page);
            }
        };
    }
}
