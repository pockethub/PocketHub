package com.github.mobile.android.issue;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static com.github.mobile.android.RequestCodes.ISSUE_CREATE;
import static com.github.mobile.android.RequestCodes.ISSUE_FILTER_EDIT;
import static com.github.mobile.android.RequestCodes.ISSUE_VIEW;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE_FILTER;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY;
import static com.google.common.collect.Lists.newArrayList;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.R.menu;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.RequestFuture;
import com.github.mobile.android.ThrowableLoader;
import com.github.mobile.android.persistence.AccountDataManager;
import com.github.mobile.android.ui.ResourceLoadingIndicator;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.github.mobile.android.util.AvatarHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;

import roboguice.inject.InjectExtra;

/**
 * Fragment to display a list of issues
 */
public class IssuesFragment extends ListLoadingFragment<Issue> {

    @Inject
    private AccountDataManager cache;

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    @InjectExtra(value = EXTRA_ISSUE_FILTER, optional = true)
    private IssueFilter filter;

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    private boolean hasMore = true;

    private final List<IssuePager> pagers = newArrayList();

    private ResourceLoadingIndicator loadingIndicator;

    private TextView filterTextView;

    @Inject
    private AvatarHelper avatarHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (filter == null)
            filter = new IssueFilter(repository);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingIndicator = new ResourceLoadingIndicator(getActivity(), string.loading_more_issues);
        loadingIndicator.setList(getListView());

        View filterHeader = getLayoutInflater(savedInstanceState).inflate(layout.issue_filter_header, null);
        filterTextView = (TextView) filterHeader.findViewById(id.tv_filter_summary);
        getListView().addHeaderView(filterHeader, null, false);
        updateFilterSummary();
    }

    private void updateFilterSummary() {
        CharSequence display = filter.toDisplay();
        if (display.length() > 0) {
            filterTextView.setText(display);
            filterTextView.setVisibility(VISIBLE);
        } else
            filterTextView.setVisibility(GONE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(string.no_issues));
        getListView().setFastScrollEnabled(true);
        getListView().setOnScrollListener(new OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!hasMore)
                    return;
                if (getLoaderManager().hasRunningLoaders())
                    return;
                int size = 0;
                for (IssuePager pager : pagers)
                    size += pager.size();
                if (getListView().getLastVisiblePosition() >= size)
                    showMore();
            }
        });
    }

    @Override
    public void refresh() {
        for (IssuePager pager : pagers)
            pager.reset();
        hasMore = true;
        super.refresh();
    }

    /**
     * Show more issues while retaining the current {@link IssuePager} state
     */
    private void showMore() {
        super.refresh();
    }

    @Override
    public void onLoadFinished(Loader<List<Issue>> loader, final List<Issue> items) {
        if (hasMore)
            loadingIndicator.showLoading();
        else
            loadingIndicator.setVisible(false);

        super.onLoadFinished(loader, items);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Issue issue = (Issue) l.getItemAtPosition(position);
        startActivityForResult(ViewIssueActivity.createIntent(issue), ISSUE_VIEW);
    }

    @Override
    public Loader<List<Issue>> onCreateLoader(int i, Bundle bundle) {
        // Load pagers if needed
        if (filter != null && pagers.isEmpty())
            for (final Map<String, String> query : filter)
                pagers.add(new IssuePager(store) {

                    public PageIterator<Issue> createIterator(int page, int size) {
                        return service.pageIssues(repository, query, page, size);
                    }
                });
        final IssuePager[] loaderPagers = pagers.toArray(new IssuePager[pagers.size()]);
        return new ThrowableLoader<List<Issue>>(getActivity(), listItems) {

            @Override
            public List<Issue> loadData() throws IOException {
                boolean hasMore = false;
                final List<Issue> all = newArrayList();
                for (IssuePager pager : loaderPagers) {
                    hasMore |= pager.next();
                    all.addAll(pager.getResources());
                }
                Collections.sort(all, new CreatedAtComparator());
                IssuesFragment.this.hasMore = hasMore;
                return all;
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<Issue> adapterFor(List<Issue> items) {
        return new ViewHoldingListAdapter<Issue>(items, viewInflatorFor(getActivity(), layout.repo_issue_list_item),
                reflectiveFactoryFor(RepoIssueViewHolder.class, avatarHelper,
                        RepoIssueViewHolder.computeMaxDigits(items)));
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(menu.issues, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.create_issue:
            startActivityForResult(CreateIssueActivity.createIntent(new RepositoryId(repository.getOwner().getLogin(),
                    repository.getName())), ISSUE_CREATE);
            return true;
        case id.filter_issues:
            startActivityForResult(FilterIssuesActivity.createIntent(repository, filter), ISSUE_FILTER_EDIT);
            return true;
        case id.bookmark_filter:
            cache.addIssueFilter(filter, new RequestFuture<IssueFilter>() {

                public void success(IssueFilter response) {
                    Toast.makeText(getActivity().getApplicationContext(), "Issue filter saved to bookmarks",
                            LENGTH_LONG).show();
                }
            });
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == ISSUE_FILTER_EDIT && data != null) {
            IssueFilter newFilter = ((IssueFilter) data.getSerializableExtra(EXTRA_ISSUE_FILTER)).clone();
            if (!filter.equals(newFilter)) {
                filter = newFilter;
                updateFilterSummary();
                pagers.clear();
                refresh();
            }
        }

        if (requestCode == ISSUE_VIEW) {
            ListAdapter adapter = getListAdapter();
            if (adapter instanceof BaseAdapter)
                ((BaseAdapter) adapter).notifyDataSetChanged();
        }

        if (requestCode == ISSUE_CREATE && resultCode == RESULT_OK)
            refresh();

        super.onActivityResult(requestCode, resultCode, data);
    }
}
