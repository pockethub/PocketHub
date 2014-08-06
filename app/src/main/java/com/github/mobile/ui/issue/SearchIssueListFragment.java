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
package com.github.mobile.ui.issue;

import static android.app.SearchManager.APP_DATA;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import static org.eclipse.egit.github.core.service.IssueService.STATE_CLOSED;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.R.string;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.ui.ItemListFragment;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchIssue;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Fragment to display a list of {@link SearchIssue} instances
 */
public class SearchIssueListFragment extends ItemListFragment<SearchIssue>
        implements Comparator<SearchIssue> {

    @Inject
    private IssueService service;

    @Inject
    private AvatarLoader avatars;

    private Repository repository;

    private String query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle appData = getActivity().getIntent().getBundleExtra(APP_DATA);
        if (appData != null)
            repository = (Repository) appData.getSerializable(EXTRA_REPOSITORY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_issues);
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
        final SearchIssue searchIssue = (SearchIssue) l
                .getItemAtPosition(position);
        Issue issue = new Issue().setNumber(searchIssue.getNumber());
        startActivity(IssuesViewActivity.createIntent(issue, repository));
    }

    @Override
    public Loader<List<SearchIssue>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<SearchIssue>>(getActivity(), items) {

            public List<SearchIssue> loadData() throws Exception {
                if (repository == null)
                    return Collections.emptyList();
                List<SearchIssue> matches = new ArrayList<SearchIssue>();
                matches.addAll(service.searchIssues(repository, STATE_OPEN,
                        query));
                matches.addAll(service.searchIssues(repository, STATE_CLOSED,
                        query));
                Collections.sort(matches, SearchIssueListFragment.this);
                return matches;
            }
        };
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return string.error_issues_load;
    }

    @Override
    protected SingleTypeAdapter<SearchIssue> createAdapter(
            List<SearchIssue> items) {
        return new SearchIssueListAdapter(getActivity().getLayoutInflater(),
                items.toArray(new SearchIssue[items.size()]), avatars);
    }

    @Override
    public int compare(SearchIssue lhs, SearchIssue rhs) {
        return rhs.getNumber() - lhs.getNumber();
    }
}
