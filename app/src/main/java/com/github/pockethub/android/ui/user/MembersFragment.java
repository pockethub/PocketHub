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
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.User;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ThrowableLoader;
import com.github.pockethub.android.ui.ItemListFragment;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.service.organizations.OrganizationMemberService;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static com.github.pockethub.android.Intents.EXTRA_USER;

/**
 * Fragment to display the members of an org.
 */
public class MembersFragment extends ItemListFragment<User> {

    private User org;

    @Inject
    private AvatarLoader avatars;

    @Override
    public void onSaveInstanceState(Bundle outState) {
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
        setEmptyText(R.string.no_members);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<User>>(getActivity(), items) {

            @Override
            public List<User> loadData() throws Exception {
                OrganizationMemberService service = ServiceGenerator.createService(getContext(), OrganizationMemberService.class);

                int current = 1;
                int last = -1;
                List<User> users = new ArrayList<>();

                while (current != last){
                    Page<User> page = service.getMembers(org.login(), current).blockingGet().body();
                    users.addAll(page.items());
                    last = page.last() != null ? page.last() : -1;
                    current = page.next() != null ? page.next() : -1;
                }
                return users;
            }
        };
    }

    @Override
    protected SingleTypeAdapter<User> createAdapter(List<User> items) {
        User[] users = items.toArray(new User[items.size()]);
        return new UserListAdapter(getActivity().getLayoutInflater(), users,
                avatars);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        User user = (User) l.getItemAtPosition(position);
        startActivity(UserViewActivity.createIntent(user));
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_members_load;
    }
}
