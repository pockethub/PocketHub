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
package com.github.mobile.ui.repo;

import static java.util.Locale.US;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filter.FilterListener;
import android.widget.ListView;

import com.github.mobile.R.string;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.ui.HeaderFooterListAdapter;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemListFragment;
import com.github.mobile.ui.ItemView;
import com.github.mobile.ui.TextWatcherAdapter;
import com.github.mobile.ui.user.OrganizationSelectionListener;
import com.github.mobile.ui.user.OrganizationSelectionProvider;
import com.github.mobile.util.ViewUtils;
import com.google.inject.Inject;
import com.viewpagerindicator.R.id;
import com.viewpagerindicator.R.layout;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Fragment to display a list of {@link Repository} instances
 */
public class RepositoryListFragment extends ItemListFragment<Repository>
        implements OrganizationSelectionListener {

    @Inject
    private AccountDataManager cache;

    private final AtomicReference<User> org = new AtomicReference<User>();

    private RecentRepositories recentRepos;

    private EditText filter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(layout.item_filter_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Filter adapterFilter = getListAdapter().getWrappedAdapter()
                .getFilter();
        if (adapterFilter != null) {
            filter = (EditText) view.findViewById(id.et_filter);
            filter.addTextChangedListener(new TextWatcherAdapter() {

                @Override
                public void afterTextChanged(Editable s) {
                    adapterFilter.filter(s, new FilterListener() {

                        @Override
                        public void onFilterComplete(int count) {
                            updateHeaders(getAdapterItems());
                        }
                    });
                }

            });
        }
    }

    private List<Repository> getAdapterItems() {
        DefaultRepositoryListAdapter adapter = getRepositoryAdapter();
        if (adapter != null)
            return adapter.getItems();
        else
            return Collections.emptyList();
    }

    @Override
    public ItemListFragment<Repository> setListShown(boolean shown,
            boolean animate) {
        ViewUtils.setGone(filter, !shown);

        return super.setListShown(shown, animate);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setDividerHeight(0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        User currentOrg = ((OrganizationSelectionProvider) activity)
                .addListener(this);
        org.set(currentOrg);
        if (currentOrg != null)
            recentRepos = new RecentRepositories(activity, currentOrg);
    }

    @Override
    public void onDetach() {
        OrganizationSelectionProvider selectionProvider = (OrganizationSelectionProvider) getActivity();
        if (selectionProvider != null)
            selectionProvider.removeListener(this);

        super.onDetach();
    }

    @Override
    public void onOrganizationSelected(final User organization) {
        User previousOrg = org.get();
        int previousOrgId = previousOrg != null ? previousOrg.getId() : -1;
        org.set(organization);

        if (recentRepos != null)
            recentRepos.saveAsync();

        Activity activity = getActivity();
        if (activity != null && previousOrgId != organization.getId())
            recentRepos = new RecentRepositories(activity, organization);

        // Only hard refresh if view already created and org is changing
        if (previousOrgId != organization.getId())
            refreshWithProgress();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_repositories);
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Repository repo = (Repository) list.getItemAtPosition(position);
        if (recentRepos != null)
            recentRepos.add(repo);
        startActivity(RepositoryViewActivity.createIntent(repo));
    }

    @Override
    public void onStop() {
        super.onStop();

        if (recentRepos != null)
            recentRepos.saveAsync();
    }

    private DefaultRepositoryListAdapter getRepositoryAdapter() {
        HeaderFooterListAdapter<?> rootAdapter = getListAdapter();
        if (rootAdapter != null)
            return (DefaultRepositoryListAdapter) rootAdapter
                    .getWrappedAdapter();
        else
            return null;
    }

    private void updateHeaders(final List<Repository> repos) {
        final DefaultRepositoryListAdapter adapter = getRepositoryAdapter();
        if (adapter == null)
            return;

        adapter.clearHeaders();

        char start = 'a';
        Repository previous = null;
        for (int i = 0; i < repos.size(); i++) {
            Repository repository = repos.get(i);

            if (recentRepos.contains(repository.getId())) {
                previous = repository;
                continue;
            }

            char repoStart = Character.toLowerCase(repository.getName().charAt(
                    0));
            if (repoStart < start) {
                previous = repository;
                continue;
            }

            adapter.registerHeader(repository, Character.toString(repoStart)
                    .toUpperCase(US));
            if (previous != null)
                adapter.registerNoSeparator(previous);
            start = repoStart;
            if (start == 'z')
                break;
            start++;
            previous = repository;
        }

        if (!repos.isEmpty()) {
            Repository first = repos.get(0);
            if (recentRepos.contains(first))
                adapter.registerHeader(first, getString(string.recently_viewed));
        }
    }

    @Override
    public Loader<List<Repository>> onCreateLoader(int id, final Bundle args) {
        return new ThrowableLoader<List<Repository>>(getActivity(), items) {

            @Override
            public List<Repository> loadData() throws Exception {
                User org = RepositoryListFragment.this.org.get();
                if (org == null)
                    return Collections.emptyList();

                List<Repository> repos = cache.getRepos(org,
                        isForceRefresh(args));
                Collections.sort(repos, recentRepos);
                updateHeaders(repos);
                return repos;
            }
        };
    }

    @Override
    protected ItemListAdapter<Repository, ? extends ItemView> createAdapter(
            List<Repository> items) {
        return new DefaultRepositoryListAdapter(getActivity()
                .getLayoutInflater(), items, org);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return string.error_repos_load;
    }
}
