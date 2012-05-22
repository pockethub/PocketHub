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

import android.app.Activity;

import com.github.mobile.core.user.UserEventMatcher.UserPair;
import com.github.mobile.ui.NewsFragment;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Fragment to display a news feed for a given user/org
 */
public abstract class UserNewsFragment extends NewsFragment implements OrganizationSelectionListener {

    /**
     * Current organization/user
     */
    protected User org;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        org = ((OrganizationSelectionProvider) activity).addListener(this);
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
    protected void viewUser(UserPair users) {
        if (org.getId() != users.from.getId())
            startActivity(UserViewActivity.createIntent(users.from));
        else if (org.getId() != users.to.getId())
            startActivity(UserViewActivity.createIntent(users.to));
    }
}
