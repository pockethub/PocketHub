package com.github.mobile.android.ui.user;

import android.os.Bundle;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ResourcePager;
import com.github.mobile.android.ui.PagedListFragment;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.ListViewHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.UserService;

/**
 * Fragment to display a list of followers
 */
public class FollowersFragment extends PagedListFragment<User> {

    @Inject
    private AvatarHelper avatarHelper;

    @Inject
    private UserService userService;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListViewHelper.configure(getActivity(), getListView(), true);
    }

    @Override
    protected ResourcePager<User> createPager() {
        return new ResourcePager<User>() {

            protected Object getId(User resource) {
                return resource.getId();
            }

            public PageIterator<User> createIterator(int page, int size) {
                return userService.pageFollowers(page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_followers;
    }

    protected ViewHoldingListAdapter<User> adapterFor(List<User> items) {
        return new ViewHoldingListAdapter<User>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.user_list_item),
                ReflectiveHolderFactory.reflectiveFactoryFor(UserViewHolder.class, avatarHelper));
    }

}
