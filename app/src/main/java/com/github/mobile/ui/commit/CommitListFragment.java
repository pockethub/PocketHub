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
package com.github.mobile.ui.commit;

import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import static com.github.mobile.RequestCodes.COMMIT_VIEW;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.core.ResourcePager;
import com.github.mobile.core.commit.CommitPager;
import com.github.mobile.core.commit.CommitStore;
import com.github.mobile.ui.PagedItemFragment;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;

import roboguice.inject.InjectExtra;

/**
 * Fragment to display a list of repository commits
 */
public class CommitListFragment extends PagedItemFragment<RepositoryCommit> {

    /**
     * Avatar loader
     */
    @Inject
    protected AvatarLoader avatars;

    @Inject
    private CommitService service;

    @Inject
    private CommitStore store;

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_commits);
    }

    @Override
    protected ResourcePager<RepositoryCommit> createPager() {
        return new CommitPager(repository, store) {

            private String last;

            @Override
            protected RepositoryCommit register(RepositoryCommit resource) {
                // Store first parent of last commit registered for next page
                // lookup
                List<Commit> parents = resource.getParents();
                if (parents != null && !parents.isEmpty())
                    last = parents.get(0).getSha();
                else
                    last = null;

                return super.register(resource);
            }

            @Override
            public PageIterator<RepositoryCommit> createIterator(int page,
                    int size) {
                return service.pageCommits(repository, last, null, size);
            }

            @Override
            public ResourcePager<RepositoryCommit> clear() {
                last = null;
                return super.clear();
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_commits;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return string.error_commits_load;
    }

    @Override
    protected SingleTypeAdapter<RepositoryCommit> createAdapter(
            List<RepositoryCommit> items) {
        return new CommitListAdapter(layout.commit_item, getActivity()
                .getLayoutInflater(), items, avatars);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Object item = l.getItemAtPosition(position);
        if (item instanceof RepositoryCommit)
            startActivityForResult(CommitViewActivity.createIntent(repository,
                    position, items), COMMIT_VIEW);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == COMMIT_VIEW) {
            notifyDataSetChanged();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
