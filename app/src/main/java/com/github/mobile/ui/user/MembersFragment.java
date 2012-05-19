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
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.R.string;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.accounts.AccountUtils;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemListFragment;
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OrganizationService;

/**
 * Fragment to display the members of an org.
 */
public class MembersFragment extends ItemListFragment<User> implements OrganizationSelectionListener {

    private User org;

    @Inject
    private OrganizationService service;

    @Inject
    private AvatarLoader avatarHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        org = ((OrganizationSelectionProvider) activity).addListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_members);
    }

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<User>>(getActivity(), items) {

            public List<User> loadData() throws Exception {
                return service.getMembers(org.getLogin());
            }
        };
    }

    @Override
    protected ItemListAdapter<User, ? extends ItemView> createAdapter(List<User> items) {
        User[] users = items.toArray(new User[items.size()]);
        return new UserListAdapter(getActivity().getLayoutInflater(), users, avatarHelper);
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        User user = (User) l.getItemAtPosition(position);
        if (AccountUtils.isUser(getActivity(), user))
            startActivity(HomeActivity.createIntent());
        else
            startActivity(UserViewActivity.createIntent(user));
    }
}
