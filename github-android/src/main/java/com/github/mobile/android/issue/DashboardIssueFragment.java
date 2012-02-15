package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Dashboard issue list fragment
 */
public class DashboardIssueFragment extends ListLoadingFragment<Issue> {

    /**
     * Filter data argument
     */
    public static final String ARG_FILTER = "filter";

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    private Map<String, String> filterData;

    private Issue lastIssue;

    private boolean hasMore = true;

    private View showMoreFooter;

    private Button moreButton;

    private IssuePager pager = new IssuePager(store) {

        public PageIterator<Issue> createIterator(int page, int size) {
            return service.pageIssues(filterData, page, size);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        filterData = (Map<String, String>) getArguments().getSerializable(ARG_FILTER);
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public Loader<List<Issue>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader<List<Issue>>(getActivity()) {

            public List<Issue> loadInBackground() {
                try {
                    hasMore = pager.next();
                } catch (final IOException e) {
                    showError(e, string.error_issues_load);
                }
                return pager.getIssues();
            }
        };
    }

    @Override
    public void refresh() {
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
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(menu.issue_dashboard, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.refresh:
            refresh();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected ListAdapter adapterFor(List<Issue> items) {
        return new ViewHoldingListAdapter<Issue>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.dashboard_issue_list_item), ReflectiveHolderFactory.reflectiveFactoryFor(
                DashboardIssueViewHolder.class, RepoIssueViewHolder.computeMaxDigits(items)));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Issue issue = (Issue) l.getItemAtPosition(position);
        startActivity(ViewIssueActivity.viewIssueIntentFor(issue));
    }

    public void onLoadFinished(Loader<List<Issue>> loader, List<Issue> items) {
        if (hasMore) {
            if (showMoreFooter == null) {
                showMoreFooter = getActivity().getLayoutInflater().inflate(layout.show_more_item, null);
                moreButton = (Button) showMoreFooter.findViewById(id.b_show_more);
                moreButton.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        moreButton.setText(getString(string.loading_more_issues));
                        moreButton.setEnabled(false);
                        lastIssue = (Issue) getListView().getItemAtPosition(
                                getListView().getCount() - getListView().getFooterViewsCount() - 1);
                        showMore();
                    }
                });
                getListView().addFooterView(showMoreFooter);
            }
            moreButton.setEnabled(true);
            moreButton.setText(getString(string.show_more));
        } else {
            getListView().removeFooterView(showMoreFooter);
            showMoreFooter = null;
        }

        super.onLoadFinished(loader, items);

        if (lastIssue != null) {
            final int target = lastIssue.getNumber();
            lastIssue = null;
            getListView().post(new Runnable() {

                public void run() {
                    ListView view = getListView();
                    for (int i = 0; i < view.getCount() - view.getFooterViewsCount(); i++) {
                        Issue issue = (Issue) view.getItemAtPosition(i);
                        if (target == issue.getNumber()) {
                            view.setSelection(i);
                            return;
                        }
                    }
                }
            });
        }

    }
}