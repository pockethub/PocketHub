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
package com.github.pockethub.ui.user;

import android.os.Bundle;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.core.user.UserEventMatcher.UserPair;
import com.github.pockethub.ui.NewsFragment;

import static com.github.pockethub.Intents.EXTRA_USER;

/**
 * Fragment to display a news feed for a given user/org
 */
public abstract class UserNewsFragment extends NewsFragment implements
    OrganizationSelectionListener {

    /**
     * Current organization/user
     */
    protected User org;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (org != null)
            outState.putParcelable(EXTRA_USER, org);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (getActivity() instanceof OrganizationSelectionProvider)
            org = ((OrganizationSelectionProvider) getActivity()).addListener(this);

        if (getArguments() != null && getArguments().containsKey("org"))
            org = getArguments().getParcelable("org");

        if (org == null && savedInstanceState != null)
            org = (User) savedInstanceState.get(EXTRA_USER);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        if (getActivity() != null && getActivity() instanceof OrganizationSelectionProvider) {
            OrganizationSelectionProvider selectionProvider = (OrganizationSelectionProvider) getActivity();
            selectionProvider.removeListener(this);
        }

        super.onDetach();
    }

    @Override
    protected void viewRepository(Repo repository) {
        User owner = repository.owner;
        if (owner != null && org.login.equals(owner.login))
            repository.owner = org;

        super.viewRepository(repository);
    }

    @Override
    public void onOrganizationSelected(User organization) {
        int previousOrgId = org != null ? org.id : -1;
        org = organization;
        // Only hard refresh if view already created and org is changing
        if (previousOrgId != org.id)
            refreshWithProgress();
    }

    @Override
    protected boolean viewUser(User user) {
        if (org.id != user.id) {
            startActivity(UserViewActivity.createIntent(user));
            return true;
        }
        return false;
    }

    @Override
    protected void viewUser(UserPair users) {
        if (!viewUser(users.from))
            viewUser(users.to);
    }
}
