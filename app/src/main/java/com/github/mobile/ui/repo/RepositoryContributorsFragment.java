/*
 * Copyright 2013 GitHub Inc.
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
package com.github.mobile.ui.repo;

import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.R.string;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.ui.ItemListFragment;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Fragment to display a list of contributors for a specific repository
 */
public class RepositoryContributorsFragment extends ItemListFragment<Contributor> {

    /**
     * Avatar loader
     */
    @Inject
    protected AvatarLoader avatars;

    /**
     * Repository service
     */
    @Inject
    protected RepositoryService service;

    private Repository repo;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        repo = getSerializableExtra(EXTRA_REPOSITORY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_contributors);
    }

    @Override
    public Loader<List<Contributor>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<Contributor>>(getActivity(), items) {

            @Override
            public List<Contributor> loadData() throws Exception {
                return service.getContributors(repo, false);
            }
        };
    }

    @Override
    protected SingleTypeAdapter<Contributor> createAdapter(List<Contributor> items) {
        return new ContributorListAdapter(getActivity(),
            items.toArray(new Contributor[items.size()]), avatars);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return string.error_contributors_load;
    }
}
