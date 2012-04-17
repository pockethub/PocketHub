package com.github.mobile.android.ui.user;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.github.mobile.android.HomeActivity;
import com.github.mobile.android.HomeActivity.OrgSelectionListener;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.ThrowableLoader;
import com.github.mobile.android.ui.ListLoadingFragment;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.ListViewHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OrganizationService;

/**
 * Fragment to display the members of an org.
 */
public class MembersFragment extends ListLoadingFragment<User> implements OrgSelectionListener {

    private User org;

    @Inject
    private OrganizationService service;

    @Inject
    private AvatarHelper avatarHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        org = ((HomeActivity) activity).registerOrgSelectionListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListViewHelper.configure(getActivity(), getListView(), true);
    }

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<User>>(getActivity(), listItems) {

            public List<User> loadData() throws Exception {
                return service.getMembers(org.getLogin());
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<User> adapterFor(List<User> items) {
        return new ViewHoldingListAdapter<User>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.user_list_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(UserViewHolder.class, avatarHelper));
    }

    @Override
    public void onOrgSelected(User org) {
        int previousOrgId = this.org != null ? this.org.getId() : -1;
        this.org = org;
        // Only hard refresh if view already created and org is changing
        if (getView() != null && previousOrgId != org.getId())
            hideOldContentAndRefresh();
    }
}
