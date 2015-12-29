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
package com.github.pockethub.ui.repo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.ViewFinder;
import com.github.pockethub.R;
import com.github.pockethub.ThrowableLoader;
import com.github.pockethub.persistence.AccountDataManager;
import com.github.pockethub.ui.HeaderFooterListAdapter;
import com.github.pockethub.ui.ItemListFragment;
import com.github.pockethub.ui.user.OrganizationSelectionListener;
import com.github.pockethub.ui.user.OrganizationSelectionProvider;
import com.github.pockethub.ui.user.UserViewActivity;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.InfoUtils;
import com.google.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.pockethub.Intents.EXTRA_USER;
import static com.github.pockethub.RequestCodes.REPOSITORY_VIEW;
import static com.github.pockethub.ResultCodes.RESOURCE_CHANGED;
import static java.util.Locale.US;

/**
 * Fragment to display a list of {@link Repo} instances
 */
public class RepositoryListFragment extends ItemListFragment<Repo>
    implements OrganizationSelectionListener {

    @Inject
    private AccountDataManager cache;

    @Inject
    private AvatarLoader avatars;

    private final AtomicReference<User> org = new AtomicReference<>();

    private RecentRepositories recentRepos;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        User org = this.org.get();
        if (org != null)
            outState.putParcelable(EXTRA_USER, org);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setDividerHeight(0);
        updateHeaders(items);
    }

    @Override
    public void onDetach() {
        if (getActivity() != null && getActivity() instanceof OrganizationSelectionProvider) {
            OrganizationSelectionProvider selectionProvider = (OrganizationSelectionProvider) getActivity();
            selectionProvider.removeListener(this);
        }

        super.onDetach();
    }

    @Override
    public void onOrganizationSelected(final User organization) {
        User previousOrg = org.get();
        int previousOrgId = previousOrg != null ? previousOrg.id : -1;
        org.set(organization);

        if (recentRepos != null)
            recentRepos.saveAsync();

        // Only hard refresh if view already created and org is changing
        if (previousOrgId != organization.id) {
            Activity activity = getActivity();
            if (activity != null)
                recentRepos = new RecentRepositories(activity, organization);

            refreshWithProgress();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Activity activity = getActivity();
        User currentOrg = null;

        if (getActivity() instanceof OrganizationSelectionProvider)
            currentOrg = ((OrganizationSelectionProvider) activity)
                .addListener(this);

        if (getArguments() != null && getArguments().containsKey("org"))
            currentOrg = getArguments().getParcelable("org");

        if (currentOrg == null && savedInstanceState != null)
            currentOrg = savedInstanceState.getParcelable(EXTRA_USER);
        org.set(currentOrg);
        if (currentOrg != null)
            recentRepos = new RecentRepositories(activity, currentOrg);

        setEmptyText(R.string.no_repositories);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Refresh if the viewed repository was (un)starred
        if (requestCode == REPOSITORY_VIEW && resultCode == RESOURCE_CHANGED) {
            forceRefresh();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Repo repo = (Repo) list.getItemAtPosition(position);
        if (recentRepos != null)
            recentRepos.add(repo);

        startActivityForResult(RepositoryViewActivity.createIntent(repo),
            REPOSITORY_VIEW);
    }

    @Override
    public boolean onListItemLongClick(ListView list, View v, int position,
        long itemId) {
        if (!isUsable())
            return false;

        final Repo repo = (Repo) list.getItemAtPosition(position);
        if (repo == null)
            return false;

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title(InfoUtils.createRepoId(repo));
        final MaterialDialog[] dialogHolder = new MaterialDialog[1];

        View view = getActivity().getLayoutInflater().inflate(
            R.layout.repo_dialog, null);
        ViewFinder finder = new ViewFinder(view);

        final User owner = repo.owner;
        avatars.bind(finder.imageView(R.id.iv_owner_avatar), owner);
        finder.setText(R.id.tv_owner_name, getString(R.string.navigate_to_user, owner.login));
        finder.onClick(R.id.ll_owner_area, new OnClickListener() {
            public void onClick(View v) {
                dialogHolder[0].dismiss();
                viewUser(owner);
            }
        });

        if ((recentRepos != null) && (recentRepos.contains(repo))) {
            finder.find(R.id.divider).setVisibility(View.VISIBLE);
            finder.find(R.id.ll_recent_repo_area).setVisibility(View.VISIBLE);
            finder.onClick(R.id.ll_recent_repo_area, new OnClickListener() {
                public void onClick(View v) {
                    dialogHolder[0].dismiss();
                    recentRepos.remove(repo);
                    refresh();
                }
            });
        }

        builder.customView(view, false);
        MaterialDialog dialog = builder.build();
        dialogHolder[0] = dialog;
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        return true;
    }

    private void viewUser(User user) {
        if (org.get().id != user.id)
            startActivity(UserViewActivity.createIntent(user));
    }

    @Override
    public void onStop() {
        super.onStop();

        if (recentRepos != null)
            recentRepos.saveAsync();
    }

    private void updateHeaders(final List<Repo> repos) {
        HeaderFooterListAdapter<?> rootAdapter = getListAdapter();
        if (rootAdapter == null)
            return;

        DefaultRepositoryListAdapter adapter = (DefaultRepositoryListAdapter) rootAdapter
            .getWrappedAdapter();
        adapter.clearHeaders();

        if (repos.isEmpty())
            return;

        // Add recent header if at least one recent repository
        Repo first = repos.get(0);
        if (recentRepos.contains(first))
            adapter.registerHeader(first, getString(R.string.recently_viewed));

        // Advance past all recent repositories
        int index;
        Repo current = null;
        for (index = 0; index < repos.size(); index++) {
            Repo repository = repos.get(index);
            if (recentRepos.contains(repository.id))
                current = repository;
            else
                break;
        }

        if (index >= repos.size())
            return;

        if (current != null)
            adapter.registerNoSeparator(current);

        // Register header for first character
        current = repos.get(index);
        char start = Character.toLowerCase(current.name.charAt(0));
        adapter.registerHeader(current,
            Character.toString(start).toUpperCase(US));

        char previousHeader = start;
        for (index = index + 1; index < repos.size(); index++) {
            current = repos.get(index);
            char repoStart = Character.toLowerCase(current.name.charAt(0));
            if (repoStart <= start)
                continue;

            // Don't include separator for the last element of the previous
            // character
            if (previousHeader != repoStart)
                adapter.registerNoSeparator(repos.get(index - 1));

            adapter.registerHeader(current, Character.toString(repoStart)
                .toUpperCase(US));
            previousHeader = repoStart;
            start = repoStart++;
        }

        // Don't include separator for last element
        adapter.registerNoSeparator(repos.get(repos.size() - 1));
    }

    @Override
    public Loader<List<Repo>> onCreateLoader(int id, final Bundle args) {
        return new ThrowableLoader<List<Repo>>(getActivity(), items) {

            @Override
            public List<Repo> loadData() throws Exception {
                User org = RepositoryListFragment.this.org.get();
                if (org == null)
                    return Collections.emptyList();

                List<Repo> repos = cache.getRepos(org,
                    isForceRefresh(args));
                Collections.sort(repos, recentRepos);
                updateHeaders(repos);
                return repos;
            }
        };
    }

    @Override
    protected SingleTypeAdapter<Repo> createAdapter(List<Repo> items) {
        return new DefaultRepositoryListAdapter(getActivity()
            .getLayoutInflater(),
            items.toArray(new Repo[items.size()]), org);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_repos_load;
    }
}
