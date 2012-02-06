package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.NoSuchPageException;
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

    private static final String TAG = "DIF";

    @Inject
    private IssueService service;

    private Map<String, String> filterData;

    private Issue lastIssue;

    private boolean hasMore = true;

    private View showMoreFooter;

    private Button moreButton;

    private Map<String, Issue> issues = new LinkedHashMap<String, Issue>();

    private int page = 1;

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
                hasMore = false;
                PageIterator<Issue> iterator = service.pageIssues(filterData, page, -1);
                try {
                    for (Issue issue : iterator.next())
                        if (!issues.containsKey(issue.getUrl()))
                            issues.put(issue.getUrl(), issue);
                    page++;
                } catch (NoSuchPageException e) {
                    Log.d(TAG, "Exception getting issues", e);
                }
                hasMore |= iterator.hasNext();
                return new ArrayList<Issue>(issues.values());
            }
        };
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
                        moreButton.setText(getActivity().getString(string.loading_more_issues));
                        moreButton.setEnabled(false);
                        lastIssue = (Issue) getListView().getItemAtPosition(
                                getListView().getCount() - getListView().getFooterViewsCount() - 1);
                        refresh();
                    }
                });
                getListView().addFooterView(showMoreFooter);
            }
            moreButton.setEnabled(true);
            moreButton.setText(getActivity().getString(string.show_more));
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