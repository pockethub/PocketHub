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

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.github.pockethub.android.R;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.service.organizations.OrganizationMemberService;
import io.reactivex.Single;
import retrofit2.Response;

import javax.inject.Inject;

import static com.github.pockethub.android.Intents.EXTRA_USER;

/**
 * Fragment to display the members of an org.
 */
public class MembersFragment extends PagedUserFragment {

    OrganizationMemberService service = ServiceGenerator.createService(getContext(), OrganizationMemberService.class);

    private User org;

    @Inject
    protected AvatarLoader avatars;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (org != null) {
            outState.putParcelable(EXTRA_USER, org);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        org = getArguments().getParcelable("org");
        if (org == null && savedInstanceState != null) {
            org = savedInstanceState.getParcelable(EXTRA_USER);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    protected Single<Response<Page<User>>> loadData(int page) {
        return service.getMembers(org.login(), page);
    }

    @Override
    protected int getEmptyText() {
        return R.string.no_members;
    }


    @Override
    protected int getLoadingMessage() {
        return R.string.loading;
    }

    @Override
    protected int getErrorMessage() {
        return R.string.error_members_load;
    }
}
