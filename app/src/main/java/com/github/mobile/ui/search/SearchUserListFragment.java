package com.github.mobile.ui.search;

import android.os.Bundle;
import android.support.v4.content.Loader;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.ui.ItemListFragment;

import java.util.List;

import org.eclipse.egit.github.core.User;

public class SearchUserListFragment extends
        ItemListFragment<User> {

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return 0;
    }

    @Override
    protected SingleTypeAdapter<User> createAdapter(List<User> items) {
        return null;
    }
}
