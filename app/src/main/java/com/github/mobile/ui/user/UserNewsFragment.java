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
package com.github.mobile.ui.user;

import static com.github.mobile.Intents.EXTRA_USER;
import android.os.Bundle;

import com.github.mobile.core.user.UserEventMatcher.UserPair;
import com.github.mobile.ui.NewsFragment;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

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
            outState.putSerializable(EXTRA_USER, org);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        org = ((OrganizationSelectionProvider) getActivity()).addListener(this);
        if (org == null && savedInstanceState != null)
            org = (User) savedInstanceState.get(EXTRA_USER);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        OrganizationSelectionProvider selectionProvider = (OrganizationSelectionProvider) getActivity();
        if (selectionProvider != null)
            selectionProvider.removeListener(this);

        super.onDetach();
    }

    @Override
    protected void viewRepository(Repository repository) {
        User owner = repository.getOwner();
        if (owner != null && org.getLogin().equals(owner.getLogin()))
            repository.setOwner(org);

        super.viewRepository(repository);
    }

    @Override
    public void onOrganizationSelected(User organization) {
        int previousOrgId = org != null ? org.getId() : -1;
        org = organization;
        // Only hard refresh if view already created and org is changing
        if (previousOrgId != org.getId())
            refreshWithProgress();
    }

    @Override
    protected boolean viewUser(User user) {
        if (org.getId() != user.getId()) {
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
