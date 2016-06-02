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
package com.github.pockethub.ui.issue;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.search.IssuesSearchClient;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.ThrowableLoader;
import com.github.pockethub.ui.ItemListFragment;
import com.github.pockethub.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.SearchManager.APP_DATA;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY;

/**
 * Fragment to display a list of {@link Issue} instances
 */
public class SearchIssueListFragment extends ItemListFragment<Issue>
        implements Comparator<Issue> {

    @Inject
    private AvatarLoader avatars;

    private Repo repository;

    private String query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle appData = getActivity().getIntent().getBundleExtra(APP_DATA);
        if (appData != null)
            repository = (Repo) appData.getParcelable(EXTRA_REPOSITORY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_issues);
    }

    /**
     * @param query
     * @return this fragment
     */
    public SearchIssueListFragment setQuery(final String query) {
        this.query = query;
        return this;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Issue searchIssue = (Issue) l.getItemAtPosition(position);

        startActivity(IssuesViewActivity.createIntent(searchIssue, repository));
    }

    @Override
    public Loader<List<Issue>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<Issue>>(getActivity(), items) {

            public List<Issue> loadData() throws Exception {
                if (repository == null)
                    return Collections.emptyList();
                List<Issue> matches = new ArrayList<>();

                int page = 1;
                boolean hasMore = true;
                while(hasMore){
                    hasMore = matches.addAll(new IssuesSearchClient(query, page).observable().toBlocking().first().first);
                    page++;
                }
                Collections.sort(matches, SearchIssueListFragment.this);
                return matches;
            }
        };
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_issues_load;
    }

    @Override
    protected SingleTypeAdapter<Issue> createAdapter(
            List<Issue> items) {
        return new SearchIssueListAdapter(getActivity().getLayoutInflater(),
                items.toArray(new Issue[items.size()]), avatars);
    }

    @Override
    public int compare(Issue lhs, Issue rhs) {
        return rhs.number - lhs.number;
    }
}
