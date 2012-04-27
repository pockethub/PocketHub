package com.github.mobile.ui.user;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.github.mobile.HomeActivity;
import com.github.mobile.HomeActivity.OrgSelectionListener;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemListFragment;
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.AvatarUtils;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OrganizationService;

/**
 * Fragment to display the members of an org.
 */
public class MembersFragment extends ItemListFragment<User> implements OrgSelectionListener {

    private User org;

    @Inject
    private OrganizationService service;

    @Inject
    private AvatarUtils avatarHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        org = ((HomeActivity) activity).registerOrgSelectionListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListViewUtils.configure(getActivity(), getListView(), true);
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
    public void onOrgSelected(User org) {
        int previousOrgId = this.org != null ? this.org.getId() : -1;
        this.org = org;
        // Only hard refresh if view already created and org is changing
        if (getView() != null && previousOrgId != org.getId())
            refreshWithProgress();
    }

    @Override
    protected ItemListAdapter<User, ? extends ItemView> createAdapter(List<User> items) {
        User[] users = items.toArray(new User[items.size()]);
        return new UserListAdapter(getActivity().getLayoutInflater(), users, avatarHelper);
    }
}
