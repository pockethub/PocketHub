package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ThrowableLoader;
import com.github.mobile.android.ui.ResourceLoadingIndicator;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.github.mobile.android.util.AvatarHelper;
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

    private IssuePager pager;

    @Inject
    private AvatarHelper avatarHelper;

    private ResourceLoadingIndicator loadingIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        pager = new IssuePager(store) {

            public PageIterator<Issue> createIterator(int page, int size) {
                return service.pageIssues(filterData, page, size);
            }
        };
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingIndicator = new ResourceLoadingIndicator(getActivity(), string.loading_issues);
        loadingIndicator.setList(getListView());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        filterData = (Map<String, String>) getArguments().getSerializable(ARG_FILTER);
        getListView().setFastScrollEnabled(true);
        getListView().setOnScrollListener(new OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!pager.hasMore())
                    return;
                if (getLoaderManager().hasRunningLoaders())
                    return;
                if (getListView().getLastVisiblePosition() >= pager.size())
                    showMore();
            }
        });
    }

    @Override
    public Loader<List<Issue>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<Issue>>(getActivity(), listItems) {

            public List<Issue> loadData() throws IOException {
                pager.next();
                return pager.getResources();
            }
        };
    }

    @Override
    public void refresh() {
        pager.reset();
        super.refresh();
    }

    /**
     * Show more issues while retaining the current {@link IssuePager} state
     */
    private void showMore() {
        super.refresh();
    }

    @Override
    protected ViewHoldingListAdapter<Issue> adapterFor(List<Issue> items) {
        return new ViewHoldingListAdapter<Issue>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.dashboard_issue_list_item), ReflectiveHolderFactory.reflectiveFactoryFor(
                DashboardIssueViewHolder.class, avatarHelper, RepoIssueViewHolder.computeMaxDigits(items)));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Issue issue = (Issue) l.getItemAtPosition(position);
        startActivity(ViewIssueActivity.viewIssueIntentFor(issue));
    }

    public void onLoadFinished(Loader<List<Issue>> loader, List<Issue> items) {
        if (pager.hasMore())
            loadingIndicator.showLoading();
        else
            loadingIndicator.setVisible(false);

        super.onLoadFinished(loader, items);
    }
}