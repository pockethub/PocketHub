package com.github.mobile.android.issue;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Issue;
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
                    return service.getIssues(filterData);
                } catch (IOException e) {
                    Log.d(TAG, "Exception getting issues", e);
                }
                return Collections.emptyList();
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
}