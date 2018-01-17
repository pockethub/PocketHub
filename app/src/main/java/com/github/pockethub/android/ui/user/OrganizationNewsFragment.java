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

import com.github.pockethub.android.accounts.AccountUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.service.activity.EventService;

import io.reactivex.Single;
import retrofit2.Response;

/**
 * Fragment to display an organization's news
 */
public class OrganizationNewsFragment extends UserNewsFragment {

    EventService service = ServiceGenerator.createService(getContext(), EventService.class);

    @Override
    protected Single<Response<Page<GitHubEvent>>> loadData(int page) {
        String account = AccountUtils.getLogin(getActivity());
        return service.getOrganizationEvents(account, org.login(), page);
    }
}
