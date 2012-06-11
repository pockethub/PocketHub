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
import android.view.View;
import android.widget.ListView;

import com.github.mobile.R.string;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemListFragment;
import com.github.mobile.ui.ItemView;
import com.github.mobile.ui.user.OrganizationSelectionListener;
import com.github.mobile.ui.user.OrganizationSelectionProvider;
import com.google.inject.Inject;

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
        refresh();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (recentRepos != null)
            recentRepos.saveAsync();
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

                DefaultRepositoryListAdapter adapter = (DefaultRepositoryListAdapter) getListAdapter()
                        .getWrappedAdapter();
                adapter.clearHeaders();

                char start = 'a';
                Repository previous = null;
                for (int i = 0; i < repos.size(); i++) {
                    Repository repository = repos.get(i);

                    if (recentRepos.contains(repository.getId())) {
                        previous = repository;
                        continue;
                    }

                    char repoStart = Character.toLowerCase(repository.getName()
                            .charAt(0));
                    if (repoStart < start) {
                        previous = repository;
                        continue;
                    }

                    adapter.registerHeader(repository,
                            Character.toString(repoStart).toUpperCase(US));
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
                        adapter.registerHeader(first,
                                getString(string.recently_viewed));
                }

                return repos;
            }
        };
    }

    @Override
    protected ItemListAdapter<Repository, ? extends ItemView> createAdapter(
            List<Repository> items) {
        return new DefaultRepositoryListAdapter(getActivity()
                .getLayoutInflater(),
                items.toArray(new Repository[items.size()]), org);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return string.error_repos_load;
    }
}
