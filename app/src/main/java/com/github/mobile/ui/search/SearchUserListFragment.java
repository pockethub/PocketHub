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
