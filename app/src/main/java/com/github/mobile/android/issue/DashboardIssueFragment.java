package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.async.AuthenticatedUserLoader;
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

    private boolean hasMore = true;

    private View showMoreFooter;

    private Button moreButton;

    private IssuePager pager;

    @Inject
    private AvatarHelper avatarHelper;

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

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        filterData = (Map<String, String>) getArguments().getSerializable(ARG_FILTER);
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public Loader<List<Issue>> onCreateLoader(int id, Bundle args) {
        return new AuthenticatedUserLoader<List<Issue>>(getActivity()) {

            public List<Issue> load() {
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
        if (hasMore) {
            if (showMoreFooter == null) {
                showMoreFooter = getActivity().getLayoutInflater().inflate(layout.show_more_item, null);
                moreButton = (Button) showMoreFooter.findViewById(id.b_show_more);
                moreButton.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        moreButton.setText(getString(string.loading_more_issues));
                        moreButton.setEnabled(false);
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
    }
}