package com.github.mobile.ui.user;

import android.os.Bundle;

import com.github.mobile.R.string;
import com.github.mobile.ResourcePager;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;
import com.github.mobile.ui.PagedItemFragment;
import com.github.mobile.util.AvatarUtils;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.UserService;

/**
 * Fragment to display a list of followers
 */
public class FollowersFragment extends PagedItemFragment<User> {

    @Inject
    private AvatarUtils avatarHelper;

    @Inject
    private UserService userService;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListViewUtils.configure(getActivity(), getListView(), true);
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

    @Override
    protected ItemListAdapter<User, ? extends ItemView> createAdapter(List<User> items) {
        User[] users = items.toArray(new User[items.size()]);
        return new UserListAdapter(getActivity().getLayoutInflater(), users, avatarHelper);
    }
}
