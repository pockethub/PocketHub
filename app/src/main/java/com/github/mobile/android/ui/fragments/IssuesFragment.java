package com.github.mobile.android.ui.fragments;

import static com.github.mobile.android.R.layout.issue_list_item;
import static com.github.mobile.android.issue.ViewIssueActivity.viewIssueIntentFor;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.views.IssueViewHolder;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

public class IssuesFragment extends ListLoadingFragment<Issue> {

    private final static String TAG = "IssuesF";

    @Inject
    IssueService issueService;

    @Override
    protected ViewHoldingListAdapter<Issue> adapterFor(List<Issue> issues) {
        return new ViewHoldingListAdapter<Issue>(issues, viewInflatorFor(getActivity(), issue_list_item),
                reflectiveFactoryFor(IssueViewHolder.class));
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Issue issue = (Issue) list.getItemAtPosition(position);
        startActivity(viewIssueIntentFor(issue));
    }

    @Override
    public Loader<List<Issue>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncLoader<List<Issue>>(getActivity()) {
            @Override
            public List<Issue> loadInBackground() {
                Log.i(TAG, "started loadInBackground");
                try {
                    return issueService.getIssues();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
