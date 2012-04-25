package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ResourcePager;
import com.github.mobile.android.ui.PagedListFragment;
import com.github.mobile.android.ui.issue.ViewIssuesActivity;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.ListViewHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Dashboard issue list fragment
 */
public class DashboardIssueFragment extends PagedListFragment<Issue> {

    /**
     * Filter data argument
     */
    public static final String ARG_FILTER = "filter";

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    private Map<String, String> filterData;

    @Inject
    private AvatarHelper avatarHelper;

    private final AtomicInteger numberWidth = new AtomicInteger();

    private TextView numberView;

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
        ListViewHelper.configure(getActivity(), getListView(), true);
    }

    @Override
    protected ViewHoldingListAdapter<Issue> adapterFor(List<Issue> items) {
        ViewInflator inflator = ViewInflator.viewInflatorFor(getActivity(), layout.dashboard_issue_list_item);
        numberView = (TextView) inflator.createBlankView().findViewById(id.tv_issue_number);
        return new ViewHoldingListAdapter<Issue>(items, inflator, ReflectiveHolderFactory.reflectiveFactoryFor(
                DashboardIssueViewHolder.class, avatarHelper, numberWidth));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivity(ViewIssuesActivity.createIntent(listItems, position - l.getHeaderViewsCount()));
    }

    @Override
    public void onLoadFinished(Loader<List<Issue>> loader, List<Issue> items) {
        numberWidth.set(RepoIssueViewHolder.measureNumberWidth(numberView, items));

        super.onLoadFinished(loader, items);
    }

    @Override
    protected ResourcePager<Issue> createPager() {
        return new IssuePager(store) {

            public PageIterator<Issue> createIterator(int page, int size) {
                return service.pageIssues(filterData, page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_issues;
    }
}